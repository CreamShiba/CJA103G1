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
          @RequestParam("memId") Integer memId,
          @RequestParam("type") String type,
          HttpServletRequest req,
          HttpServletResponse res) throws IOException {

    res.setContentType("image/png");
    ServletOutputStream out = res.getOutputStream();

    try {
      MembersVO member = memSvc.getOneMember(memId);
      byte[] imageBytes = null;

      if ("memAvatar".equalsIgnoreCase(type)) {
        imageBytes = member.getMemAvatar();
      }
      if (imageBytes != null) {
        out.write(imageBytes);
      } else {
        InputStream in = getClass()
                .getClassLoader()
                .getResourceAsStream("static/images/user.png");
        out.write(in.readAllBytes());
        in.close();
      }

    } catch (Exception e) {
      InputStream in = getClass()
              .getClassLoader()
              .getResourceAsStream("static/images/user.png");
      out.write(in.readAllBytes());
      in.close();
    }
  }
}