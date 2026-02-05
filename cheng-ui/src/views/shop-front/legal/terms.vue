<template>
  <section class="legal-page">
    <div class="legal-shell">
      <header class="legal-header">
        <p class="eyebrow">CoolApps Mall</p>
        <h1>服務條款</h1>
        <p class="summary">
          歡迎使用 CoolApps 商城。當您註冊或使用本服務，即表示您已閱讀、理解並同意本條款內容。
        </p>
        <p class="updated">最後更新：2026-02-05</p>
      </header>

      <article class="legal-card">
        <section>
          <h2>1. 服務範圍</h2>
          <p>本平台提供商品瀏覽、會員註冊、下單與付款等電商服務，並得視營運需求調整功能內容。</p>
        </section>

        <section>
          <h2>2. 帳號義務</h2>
          <p>您應提供真實且可聯絡之資料，妥善保管帳號與密碼，不得轉讓或借用他人使用。</p>
        </section>

        <section>
          <h2>3. 訂單與付款</h2>
          <p>訂單成立後，請依頁面顯示之期限完成付款。若付款失敗、逾期或系統異常，平台得取消訂單並通知會員。</p>
        </section>

        <section>
          <h2>4. 退換貨與售後</h2>
          <p>商品退換貨依商品頁與法令規範辦理；若商品具時效、客製或不可退性質，將於商品頁明確揭露。</p>
        </section>

        <section>
          <h2>5. 禁止行為</h2>
          <p>您不得進行惡意下單、攻擊系統、爬取非公開資料或其他影響平台營運與他人權益之行為。</p>
        </section>

        <section>
          <h2>6. 責任限制</h2>
          <p>對於因不可抗力、第三方服務異常或網路中斷造成之損害，本平台於法律允許範圍內不負擔擔保責任。</p>
        </section>

        <section>
          <h2>7. 條款修訂</h2>
          <p>平台得隨時修訂本條款，修訂後版本公告於網站。您持續使用服務即視為同意更新內容。</p>
        </section>

        <section>
          <h2>8. 聯絡方式</h2>
          <p>若有服務條款相關問題，請透過客服信箱或商城客服管道與我們聯繫。</p>
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
    radial-gradient(circle at 85% 18%, rgba(120, 195, 255, 0.26), transparent 38%),
    linear-gradient(160deg, #f4f9ff 0%, #e8f2ff 46%, #e0edff 100%);
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
