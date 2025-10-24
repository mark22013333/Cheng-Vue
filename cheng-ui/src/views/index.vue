<template>
  <div class="app-container home">
    <el-row :gutter="20">
      <el-col :sm="24" :lg="12" style="padding-left: 20px">
        <h2>CoolApps後台管理框架</h2>
        <p>

        </p>
        <p>
          <b>目前版本:</b> <span>v{{ version }}</span>
        </p>
        <p>
          <el-tag type="danger">&yen;線上測試中</el-tag>
        </p>
      </el-col>

      <el-col :sm="24" :lg="12" style="padding-left: 50px">
        <el-row>
          <el-col :span="12">
          </el-col>
        </el-row>
        <el-row>
        </el-row>
      </el-col>
    </el-row>
    <el-divider/>
    <el-row :gutter="20">
      <el-col :xs="24" :sm="24" :md="12" :lg="8">
        <el-card class="update-log">
          <div slot="header" class="clearfix">
            <span>聯絡訊息</span>
          </div>
          <div class="body">
            <p>
              <el-button
                size="mini"
                icon="el-icon-s-home"
                plain
                @click="goTarget('https://mark22013333.github.io/')">
                部落格
              </el-button>
            </p>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12" :lg="8">
        <el-card class="update-log">
          <div slot="header" class="clearfix">
            <span>更新日誌</span>
          </div>
          <el-collapse accordion>
            <el-collapse-item title="v1.5.2 - 2025-10-25">
              <ol>
                <li>整合 Selenium 爬蟲框架。</li>
                <li>新增 CA102 爬蟲 (臺灣證券交易所-即時重大訊息)。</li>
                <li>新增 CA103 爬蟲 (臺灣證券交易所-上市公司每日收盤價)。</li>
              </ol>
            </el-collapse-item>
            <el-collapse-item title="v1.5.1 - 2025-10-22">
              <ol>
                <li>建立爬蟲模組並整合 Quartz 定時任務排程。</li>
                <li>更新「定時任務」模組的新增/修改彈窗。</li>
                <li>新增「設定方式」切換功能：「從範本選擇」或「手動輸入」。</li>
              </ol>
            </el-collapse-item>
            <el-collapse-item title="v1.4.0 - 2025-10-22">
              <ol>
                <li>導入 Flyway 實作資料庫版本控制與自動化遷移。</li>
                <li>優化報表篩選，僅顯示已使用的物品分類。</li>
              </ol>
            </el-collapse-item>
            <el-collapse-item title="v1.3.4 - 2025-10-14">
              <ol>
                <li>全面更新登入頁面視覺設計，提升使用者體驗。</li>
                <li>增強手機端掃描功能，支援快速入庫與詳情檢視。</li>
                <li>針對庫存管理主頁面進行響應式設計（RWD）優化，改善在手機等小螢幕裝置上的表格與操作按鈕佈局，提升行動裝置操作體驗。</li>
              </ol>
            </el-collapse-item>
            <el-collapse-item title="v1.2.3 - 2025-10-13">
              <ol>
                <li>Docker 容器化部署流程，進行了全面的架構調整與功能增強。</li>
              </ol>
            </el-collapse-item>
            <el-collapse-item title="v1.2.2 - 2025-10-10">
              <ol>
                <li>個人中心 (User Profile) UI/UX 全面升級。</li>
                <li>庫存管理 (Inventory) 新增圖片功能。</li>
              </ol>
            </el-collapse-item>
            <el-collapse-item title="v1.2.1 - 2025-10-05">
              <ol>
                <li>整合物品與庫存管理，並新增可拖曳調整寬度的側邊選單。</li>
              </ol>
            </el-collapse-item>
            <el-collapse-item title="v1.2.0 - 2025-10-03">
              <ol>
                <li>整合 ISBN 掃描與書籍資訊爬蟲功能。</li>
              </ol>
            </el-collapse-item>
            <el-collapse-item title="v1.1.1 - 2025-09-23">
              <ol>
                <li>新增「庫存管理」模組，包含物品管理、庫存查詢、借出管理、掃描功能與庫存報表等頁面。</li>
                <li>新增一個全域浮動的掃描按鈕元件，提升操作便利性。</li>
                <li>為掃描功能整合了 html5-qrcode 套件。</li>
              </ol>
            </el-collapse-item>
            <el-collapse-item title="v1.0.1 - 2025-09-23">
              <ol>
                <li>整合了 Jasypt，對設定檔 (application.yml) 中的敏感資訊（如資料庫密碼、Token Secret）進行加密處理。</li>
                <li>將加密演算法升級為更安全的 PBEWITHHMACSHA512ANDAES_256。</li>
              </ol>
            </el-collapse-item>
            <el-collapse-item title="v1.0.0 - 2025-09-22">
              <ol>
                <li>CoolApps前後端分離系統正式發布</li>
              </ol>
            </el-collapse-item>
          </el-collapse>
        </el-card>
      </el-col>
      <!--      <el-col :xs="24" :sm="24" :md="12" :lg="8">-->
      <!--        <el-card class="update-log">-->
      <!--          <div slot="header" class="clearfix">-->
      <!--            <span>捐贈支援</span>-->
      <!--          </div>-->
      <!--          <div class="body">-->
      <!--            <img-->
      <!--              src="@/assets/images/pay.png"-->
      <!--              alt="donate"-->
      <!--              width="100%"-->
      <!--            />-->
      <!--            <span style="display: inline-block; height: 30px; line-height: 30px"-->
      <!--            >你可以請作者喝杯咖啡表示鼓勵</span-->
      <!--            >-->
      <!--          </div>-->
      <!--        </el-card>-->
      <!--      </el-col>-->
    </el-row>
  </div>
</template>

<script>
export default {
  name: "Index",
  data() {
    return {
      // 版本號
      version: "1.5.2"
    }
  },
  methods: {
    goTarget(href) {
      window.open(href, "_blank")
    }
  }
}
</script>

<style scoped lang="scss">
.home {
  blockquote {
    padding: 10px 20px;
    margin: 0 0 20px;
    font-size: 17.5px;
    border-left: 5px solid #eee;
  }

  hr {
    margin-top: 20px;
    margin-bottom: 20px;
    border: 0;
    border-top: 1px solid #eee;
  }

  .col-item {
    margin-bottom: 20px;
  }

  ul {
    padding: 0;
    margin: 0;
  }

  font-family: "open sans", "Helvetica Neue", Helvetica, Arial, sans-serif;
  font-size: 13px;
  color: #676a6c;
  overflow-x: hidden;

  ul {
    list-style-type: none;
  }

  h4 {
    margin-top: 0px;
  }

  h2 {
    margin-top: 10px;
    font-size: 26px;
    font-weight: 100;
  }

  p {
    margin-top: 10px;

    b {
      font-weight: 700;
    }
  }

  .update-log {
    ol {
      display: block;
      list-style-type: decimal;
      margin-block-start: 1em;
      margin-block-end: 1em;
      margin-inline-start: 0;
      margin-inline-end: 0;
      padding-inline-start: 40px;
    }
  }
}
</style>

