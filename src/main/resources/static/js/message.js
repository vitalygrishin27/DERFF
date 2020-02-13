function loadMessage(message){
    if(message!=null){
        var delay_popup = 500;
        var msg_pop = document.getElementById('msg_pop');
        var msg_pop_text = document.getElementById('message');
        msg_pop_text.innerHTML=message;
        setTimeout("document.getElementById('msg_pop').style.display='block';document.getElementById('msg_pop').className += 'fadeIn';", delay_popup);
    }
}