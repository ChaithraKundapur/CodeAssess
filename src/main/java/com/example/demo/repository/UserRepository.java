//package com.example.demo.repository;
//
//
//import com.example.demo.User;
//import com.example.demo.entity.UserEntity;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
//@Repository
//public interface UserRepository extends JpaRepository<UserEntity,Integer>{
//    List<UserEntity> findByTitleContainingOrContentContaining(String text, String textAgain);
//}