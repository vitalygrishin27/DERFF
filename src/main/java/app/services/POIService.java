package app.services;

import app.Models.Game;
import app.Models.Goal;
import app.Models.Offense;
import app.Models.Player;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class POIService {
    private HSSFWorkbook workbook;
    private Map<String, CellStyle> cellStyleMap = new HashMap<>();
    private HSSFSheet sheet;

    public void fillInReport(Game game) throws IOException {
        // Создать новый документ
        workbook = new HSSFWorkbook(new FileInputStream("src/main/resources/static/Report.xls"));
        // Создает стили документа
        createAllCellStyle();
        sheet = workbook.getSheetAt(0);
        fillInTeamTitle(game.getMasterTeam().getTeamName(), "B");
        fillInTeamTitle(game.getSlaveTeam().getTeamName(), "I");
        fillInTeamPlayers(game.getMasterTeam().getPlayers(), "B");
        fillInTeamPlayers(game.getSlaveTeam().getPlayers(), "H");
        fillInDate(game.getDate());
        if (game.isResultSave()) {
            fillInGoalsCount(game.getMasterGoalsCount(), "E9");
            fillInGoalsCount(game.getSlaveGoalsCount(), "G9");
            fillInGoals(game.getGoals().stream().filter(goal -> goal.getTeam().equals(game.getMasterTeam())).collect(Collectors.toList()), "C38");
            fillInGoals(game.getGoals().stream().filter(goal -> goal.getTeam().equals(game.getSlaveTeam())).collect(Collectors.toList()), "I38");
            fillInCards(game.getOffenses().stream().filter(offense -> offense.getType().equals("YELLOW")).collect(Collectors.toList()), "YELLOW", "C46", 5);
            fillInCards(game.getOffenses().stream().filter(offense -> offense.getType().equals("RED")).collect(Collectors.toList()), "RED", "C54", 3);
        }
        saveFile();
    }

    private void fillInTeamTitle(String teamName, String columnName) {
        CellReference cr = new CellReference(columnName + 9);
        Row row = sheet.getRow(cr.getRow());
        Cell cell = row.getCell(cr.getCol());
        cell.setCellStyle(cellStyleMap.get("team"));
        cell.setCellValue(teamName.length() > 20 ? teamName.substring(0, 20) : teamName);
    }

    private void fillInTeamPlayers(Collection<Player> source, String columnName) {
        ArrayList<Player> list = new ArrayList<>(source);
        Collections.sort(list);
        for (int i = 0; i < (Math.min(list.size(), 20)); i++) {
            CellReference cr = new CellReference(columnName + (i + 13));
            Row row = sheet.getRow(cr.getRow());
            Cell cell = row.getCell(cr.getCol());
            cell.setCellStyle(cellStyleMap.get("number"));
            //  cell.setCellValue((double) (i + 1));
            cell = row.getCell(cr.getCol() + 1);
            cell.setCellValue(i < 19 ? list.get(i).getLastName() + " " + list.get(i).getFirstName() : list.get(i).getLastName() + " " + list.get(i).getFirstName() + " +" + (list.size() - 20));
        }
    }

    private void fillInDate(Date date) {
        CellReference cr = new CellReference("E5");
        Row row = sheet.getRow(cr.getRow());
        Cell cell = row.getCell(cr.getCol());
        cell.setCellStyle(cellStyleMap.get("date"));
        cell.setCellValue(date);
    }

    private void fillInGoalsCount(Integer value, String column) {
        CellReference cr = new CellReference(column);
        Row row = sheet.getRow(cr.getRow());
        Cell cell = row.getCell(cr.getCol());
        cell.setCellStyle(cellStyleMap.get("goalsCount"));
        cell.setCellValue(value.doubleValue());
    }

    private void fillInGoals(List<Goal> goals, String startColumn) {
        CellReference cr = new CellReference(startColumn);
        //replace "хв" to "кількість"
        Row row = sheet.getRow(cr.getRow() - 1);
        Cell cell = row.getCell(cr.getCol() + 1);
        cell.setCellValue("кількість");

        row = sheet.getRow(cr.getRow());
        cell = row.getCell(cr.getCol());
        Map<Player, Integer> playerPosition = new HashMap<>();
        int count = 1;
        for (Goal goal : goals
        ) {
            Player player = goal.getPlayer();
            if (playerPosition.containsKey(player)) {
                Row tempRow = sheet.getRow(cr.getRow() + playerPosition.get(player) - 1);
                Cell tempCell = tempRow.getCell(cr.getCol() + 1);
                tempCell.setCellValue(tempCell.getNumericCellValue() + 1d);
                continue;
            }

            if (count < 6) {
                playerPosition.put(player, count);
                cell.setCellValue(player.getLastName() + " " + player.getFirstName());
                cell = row.getCell(cr.getCol() + 1);
                cell.setCellStyle(cellStyleMap.get("center"));
                cell.setCellValue(1d);
                row = sheet.getRow(cr.getRow() + count);
                cell = row.getCell(cr.getCol());
                count++;
            } else if (count == 6) {
                cr = new CellReference("H45");
                row = sheet.getRow(cr.getRow());
                cell = row.getCell(cr.getCol());
                cell.setCellStyle(cellStyleMap.get("number"));
                cell.setCellValue(cell.getStringCellValue() + " Забиті м’ячі: " + player.getLastName() + " " + player.getFirstName() + " (" + player.getTeam().getTeamName() + ")");
            } else {
                cell.setCellValue(cell.getStringCellValue() + ", " + player.getLastName() + " " + player.getFirstName() + " (" + player.getTeam().getTeamName() + ")");
            }

        }
    }

    private void fillInCards(List<Offense> offenses, String offenseType, String startColumn, int maxNumber) {
        CellReference cr = new CellReference(startColumn);
        Row row = sheet.getRow(cr.getRow());
        Cell cell = row.getCell(cr.getCol());
        int count = 1;
        for (Offense offense : offenses
        ) {
            Player player = offense.getPlayer();
            if (count < maxNumber + 1) {
                cell.setCellValue(player.getLastName() + " " + player.getFirstName());
                cell = row.getCell(cr.getCol() + 1);
                cell.setCellValue(player.getTeam().getTeamName());
                row = sheet.getRow(cr.getRow() + count);
                cell = row.getCell(cr.getCol());
                count++;
            } else if (count == maxNumber + 1) {
                cr = new CellReference("H45");
                row = sheet.getRow(cr.getRow());
                cell = row.getCell(cr.getCol());
                cell.setCellStyle(cellStyleMap.get("number"));
                if (offenseType.equals("RED")) {
                    cell.setCellValue(cell.getStringCellValue() + " Червоні картки: " + player.getLastName() + " " + player.getFirstName() + " (" + player.getTeam().getTeamName() + ")");
                } else {
                    cell.setCellValue(cell.getStringCellValue() + " Жовті картки: " + player.getLastName() + " " + player.getFirstName() + " (" + player.getTeam().getTeamName() + ")");
                }
                count++;
            } else {
                cell.setCellValue(cell.getStringCellValue() + ", " + player.getLastName() + " " + player.getFirstName() + " (" + player.getTeam().getTeamName() + ")");
            }
        }
    }

    private void createAllCellStyle() {
        cellStyleMap.put("center", createCellStyleForCenter());
        cellStyleMap.put("number", createCellStyleForTitle());
        cellStyleMap.put("team", createCellStyleForTeam());
        cellStyleMap.put("date", createCellStyleForDate());
        cellStyleMap.put("goalsCount", createCellStyleForGoalsCount());
    }

    private CellStyle createCellStyleForCenter() {
        Font newFont = workbook.createFont();
        newFont.setBold(false);
        newFont.setColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
        newFont.setFontHeightInPoints((short) 14);
        newFont.setItalic(false);
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFont(newFont);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.MEDIUM);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        return cellStyle;
    }

    private CellStyle createCellStyleForTitle() {
        Font newFont = workbook.createFont();
        newFont.setBold(false);
        newFont.setColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
        newFont.setFontHeightInPoints((short) 11);
        newFont.setItalic(false);
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFont(newFont);
        cellStyle.setBorderLeft(BorderStyle.MEDIUM);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setWrapText(true);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        return cellStyle;
    }

    private CellStyle createCellStyleForTeam() {
        Font newFont = workbook.createFont();
        newFont.setBold(true);
        newFont.setColor(HSSFColor.HSSFColorPredefined.BLUE.getIndex());
        newFont.setFontHeightInPoints((short) 16);
        newFont.setItalic(false);
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFont(newFont);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setWrapText(true);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        return cellStyle;
    }

    private CellStyle createCellStyleForDate() {
        Font newFont = workbook.createFont();
        newFont.setBold(true);
        newFont.setColor(HSSFColor.HSSFColorPredefined.BLUE.getIndex());
        newFont.setFontHeightInPoints((short) 11);
        newFont.setItalic(false);
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFont(newFont);
        cellStyle.setWrapText(true);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setBorderTop(BorderStyle.MEDIUM);
        cellStyle.setBorderRight(BorderStyle.MEDIUM);
        cellStyle.setBorderBottom(BorderStyle.MEDIUM);
        cellStyle.setBorderLeft(BorderStyle.MEDIUM);
        CreationHelper createHelper = workbook.getCreationHelper();
        cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd.mm.yyyy"));
        return cellStyle;
    }

    private CellStyle createCellStyleForGoalsCount() {
        Font newFont = workbook.createFont();
        newFont.setBold(true);
        newFont.setColor(HSSFColor.HSSFColorPredefined.RED.getIndex());
        newFont.setFontHeightInPoints((short) 20);
        newFont.setItalic(false);
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFont(newFont);
        cellStyle.setBorderTop(BorderStyle.MEDIUM);
        cellStyle.setBorderRight(BorderStyle.MEDIUM);
        cellStyle.setBorderBottom(BorderStyle.MEDIUM);
        cellStyle.setBorderLeft(BorderStyle.MEDIUM);
        cellStyle.setWrapText(true);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        return cellStyle;
    }

    private void saveFile() {
        try {
            File file = new File("src/main/resources/static/Report+.xls");
            if (file.exists()) {
                file.delete();
            }
            FileOutputStream outFile = new FileOutputStream("src/main/resources/static/Report+.xls");
            workbook.write(outFile);
            outFile.close();
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
