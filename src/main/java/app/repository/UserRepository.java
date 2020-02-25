package app.repository;

import app.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User,Long> {

    @Query("Select u from User u where u.login = :login")
    User findUserByLogin(@Param("login") String login);
}
