package com.karshop.adminauth.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthService {

  @Autowired
  private AdminAuthRepository adminAuthRepository;

}