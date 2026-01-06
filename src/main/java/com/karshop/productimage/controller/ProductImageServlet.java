package com.karshop.productimage.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Date;
import java.util.LinkedList;
import java.util.List;

import com.karshop.productimage.model.ProductImageService;
import com.karshop.productimage.model.ProductImageVO;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet("/ProductImage")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 5 * 1024 * 1024, maxRequestSize = 5 * 5 * 1024 * 1024)
public class ProductImageServlet extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        doPost(req, res);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setCharacterEncoding("UTF-8");
        res.setContentType("text/html; charset=UTF-8");
        PrintWriter out = res.getWriter();
        String action = req.getParameter("action");
        System.out.println("收到請求，Action 為: " + action);

        if ("getOneImage".equals(action)) {
            List<String> errorMsgs = new LinkedList<String>();
            req.setAttribute("errorMsgs", errorMsgs);

            // 接收請求參數
            String str = req.getParameter("imgNo");
            if (str == null || (str.trim().length() == 0)) {
                errorMsgs.add("請輸入商品圖片編號");
            }
            if (!errorMsgs.isEmpty()) {
                RequestDispatcher failureView = req.getRequestDispatcher("/back_end/productimage/select_page.jsp");
                failureView.forward(req, res);
                return;
            }

            Integer imgNo = null;
            try {
                imgNo = Integer.valueOf(str);
            } catch (Exception e) {
                errorMsgs.add("商品圖片編號格式不正確");
            }
            if (!errorMsgs.isEmpty()) {
                RequestDispatcher failureView = req.getRequestDispatcher("/back_end/productimage/select_page.jsp");
                failureView.forward(req, res);
                return;
            }

            // 開始查詢資料
            ProductImageService piSvc = new ProductImageService();
            ProductImageVO piVO = piSvc.getOneImage(imgNo);
            if(piVO == null) {
                errorMsgs.add("查無資料");
            }
            if (!errorMsgs.isEmpty()) {
                RequestDispatcher failureView = req.getRequestDispatcher("/back_end/productimage/select_page.jsp");
                failureView.forward(req, res);
                return;
            }

            // 查詢完成，準備轉交
            req.setAttribute("piVO", piVO);
            String url = "/back_end/productimage/listOneProductImage.jsp";
            RequestDispatcher successView = req.getRequestDispatcher(url);
            successView.forward(req, res);
        }

        if("insert".equals(action)) {
            List<String> errorMsgs = new LinkedList<String>();
            req.setAttribute("errorMsgs", errorMsgs);

            // 接受請求參數
            String prodNoReg ="^\\d{4}$";
            String str = req.getParameter("prodNo");
            if (str == null || (str.trim().length() == 0)) {
                errorMsgs.add("商品編號: 請勿空白");
            }else if(!str.trim().matches(prodNoReg)) {
                errorMsgs.add("商品編號: 只能是數字，且長度為4");
            }

            Integer prodNo = null;
            if(errorMsgs.isEmpty()) {
                prodNo = Integer.valueOf(str);
            }

            ProductImageService piSvc = new ProductImageService();
            String prodName = (prodNo != null) ? piSvc.getProdNameByProdNo(prodNo) : "";

            if(prodName == null || prodName.trim().length() == 0) {
                errorMsgs.add("商品名稱: 請勿空白");
            }

            Part part = req.getPart("upFile");
            byte[] upFile = null;
            if(part == null || part.getSize() == 0) {
                errorMsgs.add("請上傳圖片");
            } else {
                try(InputStream in = part.getInputStream()){
                    upFile = in.readAllBytes();
                } catch(Exception e){
                    e.printStackTrace();
                    errorMsgs.add("請重新上傳");
                }
            }

            ProductImageVO piVO = new ProductImageVO();
            piVO.setProdNo(prodNo);
            piVO.setProdName(prodName);
            piVO.setUpFile(upFile);

            if(!errorMsgs.isEmpty()) {
                req.setAttribute("piVO", piVO);
                RequestDispatcher failureView = req.getRequestDispatcher("/back_end/productimage/addProductImage.jsp");
                failureView.forward(req, res);
                return;
            }

            // 開始新增資料
            piVO = piSvc.addProductImage(prodNo, prodName, upFile);

            List<ProductImageVO> piList = piSvc.getAll();
            req.setAttribute("piList", piList);

            // 新增完成，準備轉交
            String url = "/back_end/productimage/listAllProductImage.jsp";
            RequestDispatcher successView = req.getRequestDispatcher(url);
            successView.forward(req, res);
        }

        if("getAll".equals(action)) {
            ProductImageService piSvc = new ProductImageService();
            List<ProductImageVO> piList = piSvc.getAll();
            req.setAttribute("piList", piList);
            String url = "/back_end/productimage/listAllProductImage.jsp";
            RequestDispatcher successView = req.getRequestDispatcher(url);
            successView.forward(req, res);
            return;
        }

        if("update".equals(action)) {
            List<String> errorMsgs = new LinkedList<String>();
            req.setAttribute("errorMsgs", errorMsgs);

            // 接收請求參數
            Integer imgNo = Integer.valueOf(req.getParameter("imgNo").trim());
            Integer prodNo = Integer.valueOf(req.getParameter("prodNo").trim());
            String prodName = req.getParameter("prodName");

            if(prodName == null || prodName.trim().length() == 0) {
                errorMsgs.add("商品名稱: 請勿空白");
            }

            ProductImageService piSvc = new ProductImageService();
            ProductImageVO oldpiVO = piSvc.getOneImage(imgNo);

            Date uploadDate = null;
            byte[] upFile = null;

            Part part = req.getPart("upFile");
            if(part == null || part.getSize() == 0) {
                upFile = oldpiVO.getUpFile();
                uploadDate = oldpiVO.getUploadDate();
            } else {
                try(InputStream in = part.getInputStream()){
                    upFile = in.readAllBytes();
                    uploadDate = new Date(System.currentTimeMillis());
                } catch(Exception e){
                    e.printStackTrace();
                    errorMsgs.add("請重新上傳");
                }
            }

            ProductImageVO piVO = new ProductImageVO();
            piVO.setImgNo(imgNo);
            piVO.setProdNo(prodNo);
            piVO.setProdName(prodName);
            piVO.setUploadDate(uploadDate);
            piVO.setUpFile(upFile);

            if(!errorMsgs.isEmpty()) {
                req.setAttribute("piVO", piVO);
                RequestDispatcher failureView = req.getRequestDispatcher("/back_end/productimage/update_image_input.jsp");
                failureView.forward(req, res);
                return;
            }

            // 開始修改資料
            piVO = piSvc.updateProductImage(imgNo, prodNo, prodName, uploadDate, upFile);

            // 修改完成，準備轉交
            req.setAttribute("piVO", piVO);
            String url = "/back_end/productimage/listOneProductImage.jsp";
            RequestDispatcher successView = req.getRequestDispatcher(url);
            successView.forward(req, res);
        }

        if("delete".equals(action)) {
            Integer imgNo = Integer.valueOf(req.getParameter("imgNo"));
            ProductImageService piSvc = new ProductImageService();
            piSvc.deleteImage(imgNo);

            List<ProductImageVO> piList = piSvc.getAll();
            req.setAttribute("piList", piList);
            String url = "/back_end/productimage/listAllProductImage.jsp";
            RequestDispatcher successView = req.getRequestDispatcher(url);
            successView.forward(req, res);
        }

        if("getOne_For_Update".equals(action)) {
            Integer imgNo = Integer.valueOf(req.getParameter("imgNo"));

            ProductImageService piSvc = new ProductImageService();
            ProductImageVO piVO = piSvc.getOneImage(imgNo);

            req.setAttribute("piVO", piVO);
            String url = "/back_end/productimage/update_image_input.jsp";
            RequestDispatcher successView = req.getRequestDispatcher(url);
            successView.forward(req, res);
        }
    }
}
