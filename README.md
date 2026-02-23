# CJA103G1 卡爾夏汽車周邊平台

## 專案簡介
此專案追求的有以下四點：

1.解決汽車資訊不對等普遍存在：
多數車主難以掌握自己車款可使用的配件，或判斷技師的服務品質與價格是否合理。

2.降低專業知識門檻：
平台協助一般車主在不需具備深厚汽車知識的情況下，快速找到適用的配件與符合需求的技師。

3.提升透明度與便利性：
整合配件資訊、技師評價與服務內容，讓車主能更輕鬆做出選擇。

4.打造車友交流生態系：
設置論壇提供車友分享經驗、交流資訊與互相支援的平台。


## 使用技術
![Java](https://img.shields.io/badge/-Java-007396?logo=java&logoColor=white&style=flat-square)
![Spring Boot](https://img.shields.io/badge/-Spring_Boot-6DB33F?logo=spring-boot&logoColor=white&style=flat-square)
![Spring MVC](https://img.shields.io/badge/-Spring_MVC-6DB33F?logo=spring&logoColor=white&style=flat-square)
![JPA](https://img.shields.io/badge/-JPA-59666C?logo=hibernate&logoColor=white&style=flat-square)
![Hibernate](https://img.shields.io/badge/-Hibernate-59666C?logo=hibernate&logoColor=white&style=flat-square)
![MySQL](https://img.shields.io/badge/-MySQL-4479A1?logo=mysql&logoColor=white&style=flat-square)
![Redis](https://img.shields.io/badge/-Redis-DC382D?logo=redis&logoColor=white&style=flat-square)
![Maven](https://img.shields.io/badge/-Maven-C71A36?logo=apachemaven&logoColor=white&style=flat-square)
![Thymeleaf](https://img.shields.io/badge/-Thymeleaf-005F0F?logo=thymeleaf&logoColor=white&style=flat-square)
![HTML5](https://img.shields.io/badge/-HTML5-E34F26?logo=html5&logoColor=white&style=flat-square)
![CSS3](https://img.shields.io/badge/-CSS3-1572B6?logo=css3&logoColor=white&style=flat-square)
![JavaScript](https://img.shields.io/badge/-JavaScript-F7DF1E?logo=javascript&logoColor=black&style=flat-square)
![jQuery](https://img.shields.io/badge/-jQuery-0769AD?logo=jquery&logoColor=white&style=flat-square)
![AJAX](https://img.shields.io/badge/-AJAX-0081CB?logo=jquery&logoColor=white&style=flat-square)
![Fetch](https://img.shields.io/badge/-Fetch-000000?logo=javascript&logoColor=white&style=flat-square)
![Bootstrap](https://img.shields.io/badge/-Bootstrap-7952B3?logo=bootstrap&logoColor=white&style=flat-square)
![DataTables](https://img.shields.io/badge/-DataTables-1E90FF?logo=databricks&logoColor=white&style=flat-square)


## 開發協作流程(每天務必執行)
1. 開發前同步主線  
    Pull… + 選 master + Rebase  
    即使沒寫程式，也要先 rebase !!!
2. 切換到自己分支開始開發  
    Team → Switch To 自己的分支
3. 若只是進度中  
    選擇 commit + push 
    不需 PR
4. 若功能完成  
    commit + push  
    再開 PR（選 master 為目標）
5. 若 PR 無法合併（conflict）  
    PR 發起人需再次 rebase main  
    解決衝突後重新 push

## 其他注意事項
#Java package 命名建議

- 使用 `com.karshop.<功能>`，例如：
    - `com.karshop.admins`
    - `com.karshop.member`
    - `com.karshop.cart`

#檔案與資料夾結構建議

```
src
└── main
    ├── java
    │   └── com
    │       └── karshop  
    │           ├── admin      ← 自己建立資料夾，放自己的模組檔案
    │           │   ├── controller
    │           │   ├── service
    │           │   ├── model
    │           │   └── dao
    │           ├── member
    │           └── cart
    └── resources
        ├── static                  ← 放全部的css, js, images...(注意命名與分類)
        ├── templates               ← 放 Thymeleaf 的 HTML 檔案 (依照前後台>自己建立資料夾)
        │   ├── back-end
        │   │   ├── admin
        │   │   ├── member
        │   │   └── cart
        │   └── front-end
        │       ├── admin
        │       ├── member
        │       └── cart
        ├── application.properties           ← 資料庫設置，上線時統一合併，先不要動
        └── application-local.properties     ← 自己新增，設定自己的連線密碼(不會push)


```

#資料庫連線設定說明

請每位組員於 `src/main/resources` 中建立 `application-local.properties`，設定以下個人連線資訊：

```
spring.datasource.password=你的密碼
spring.mail.username=發送者的信箱
spring.mail.password=發送者的密碼(gmail需創立應用程式密碼)
```

📌 此檔案不應被 commit，已在 `.gitignore` 中排除，保障安全。
