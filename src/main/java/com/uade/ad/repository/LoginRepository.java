package com.uade.ad.repository;

import com.uade.ad.model.ScreenspaceTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginRepository extends JpaRepository<ScreenspaceTest, Integer> {
}
