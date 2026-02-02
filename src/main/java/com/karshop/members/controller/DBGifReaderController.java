package com.karshop.members.controller;

import java.io.*;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.karshop.members.model.MembersService;
import com.karshop.members.model.MembersVO;

@Controller
@RequestMapping("/members")
public class DBGifReaderController {

  @Autowired
  private MembersService memSvc;

  @GetMapping("/DBGifReader")
  public void getMemberImage(
          @RequestParam("memNo") Integer memNo,
          @RequestParam("type") String type,
          HttpServletRequest req,
          HttpServletResponse res) throws IOException {

    res.setContentType("image/png");
    ServletOutputStream out = res.getOutputStream();

    try {
      MembersVO member = memSvc.getOneMember(memNo);//檢查資料庫
      byte[] imageBytes = null;

      if ("memAvatar".equalsIgnoreCase(type)) {//判斷是否為頭像
        imageBytes = member.getMemAvatar();
      }
      if (imageBytes != null) {//如果資料庫有圖，直接輸出圖片資料
        out.write(imageBytes);
      } else {
        InputStream in = getClass()
                .getClassLoader()
                .getResourceAsStream("static/images/user.png");
        out.write(in.readAllBytes());
        in.close();
      }

    } catch (Exception e) {
      //===========防止因找不到會員或資料庫連線失敗時例外發生===========
      InputStream in = getClass()
              .getClassLoader()
              .getResourceAsStream("static/images/user.png");
      //===============================================================
      out.write(in.readAllBytes());
      in.close();
    }
  }
}