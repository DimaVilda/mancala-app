package com.vilda.mancala.mancalaapp.repository;

import com.vilda.mancala.mancalaapp.domain.TableCurrentState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TableCurrentStateRepository extends JpaRepository<TableCurrentState, String> {
}
