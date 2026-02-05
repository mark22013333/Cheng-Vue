<template>
  <section class="legal-page">
    <div class="legal-shell">
      <header class="legal-header">
        <p class="eyebrow">CoolApps Mall</p>
        <h1>隱私政策</h1>
        <p class="summary">
          我們重視您的個人資料保護，以下說明資料蒐集、使用、保存與權利行使方式。
        </p>
        <p class="updated">最後更新：2026-02-05</p>
      </header>

      <article class="legal-card">
        <section>
          <h2>1. 蒐集資料類型</h2>
          <p>為提供會員服務，我們可能蒐集暱稱、電話、電子信箱、訂單資訊與裝置連線紀錄等必要資料。</p>
        </section>

        <section>
          <h2>2. 使用目的</h2>
          <p>資料將用於會員識別、訂單處理、物流通知、客服溝通、行銷推播與服務品質改善。</p>
        </section>

        <section>
          <h2>3. 資料保存與安全</h2>
          <p>我們採取合理技術與管理措施保護資料安全，並在符合營運與法令需求期間內保存資料。</p>
        </section>

        <section>
          <h2>4. 第三方揭露</h2>
          <p>除法令要求或履行服務所需（如金流、物流）外，不會任意將您的個人資料提供給無關第三方。</p>
        </section>

        <section>
          <h2>5. Cookie 與追蹤技術</h2>
          <p>網站可能使用 Cookie 改善體驗與分析流量，您可透過瀏覽器設定管理 Cookie 偏好。</p>
        </section>

        <section>
          <h2>6. 會員權利</h2>
          <p>您可依法申請查詢、更正、刪除或停止使用個人資料；若帳號刪除，依法須保存之交易資料不在此限。</p>
        </section>

        <section>
          <h2>7. 政策更新</h2>
          <p>本政策將因法令或服務調整而更新，最新版本會公告於本頁，建議您定期查閱。</p>
        </section>

        <section>
          <h2>8. 聯絡我們</h2>
          <p>若有隱私與個資保護相關問題，請透過客服管道聯繫，我們將盡速協助處理。</p>
        </section>
      </article>
    </div>
  </section>
</template>

<style scoped>
.legal-page {
  min-height: calc(100vh - 120px);
  padding: 30px 16px 46px;
  background:
    radial-gradient(circle at 15% 12%, rgba(110, 193, 255, 0.22), transparent 36%),
    linear-gradient(160deg, #f5faff 0%, #eaf3ff 48%, #e2eeff 100%);
}

.legal-shell {
  width: min(920px, 100%);
  margin: 0 auto;
}

.legal-header {
  margin-bottom: 18px;
}

.eyebrow {
  margin: 0 0 10px;
  color: #2d69c6;
  letter-spacing: 0.12em;
  font-size: 12px;
  font-weight: 700;
}

.legal-header h1 {
  margin: 0;
  color: #123765;
  font-size: 36px;
  line-height: 1.2;
  font-family: 'Noto Serif TC', serif;
}

.summary {
  margin: 12px 0 6px;
  color: #4b678d;
  line-height: 1.8;
}

.updated {
  margin: 0;
  color: #7190b9;
  font-size: 13px;
}

.legal-card {
  background: #ffffff;
  border-radius: 20px;
  border: 1px solid #d2e3fb;
  box-shadow: 0 14px 30px rgba(28, 62, 117, 0.12);
  padding: 26px 22px;
  display: grid;
  gap: 16px;
}

.legal-card h2 {
  margin: 0 0 8px;
  color: #1c4379;
  font-size: 19px;
}

.legal-card p {
  margin: 0;
  color: #445f82;
  line-height: 1.9;
}

@media (max-width: 768px) {
  .legal-page {
    padding-top: 18px;
  }

  .legal-header h1 {
    font-size: 30px;
  }

  .legal-card {
    border-radius: 16px;
    padding: 20px 16px;
  }
}
</style>
