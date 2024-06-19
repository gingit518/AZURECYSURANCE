package com.cyberintech.vrisk.server.repository.jpa;

import com.cyberintech.vrisk.server.model.jpa.entity.Users;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<Users, Long> {

	List<Users> findAllByEmailIgnoreCase(String email);

	Optional<Users> findFirstByEmailIgnoreCase(String email);

}
