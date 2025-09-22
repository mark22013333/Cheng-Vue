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
      version: "1.0.1"
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

