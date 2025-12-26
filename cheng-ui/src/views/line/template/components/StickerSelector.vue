<template>
  <el-dialog
    v-model="visible"
    title="選擇貼圖"
    width="800px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="sticker-selector">
      <!-- 貼圖包選擇（換行顯示） -->
      <div class="package-tabs">
        <div class="tab-list">
          <div
            v-for="pkg in stickerPackages"
            :key="pkg.packageId"
            class="tab-item"
            :class="{ active: selectedPackageId === pkg.packageId }"
            @click="selectedPackageId = pkg.packageId"
          >
            <span class="tab-name">{{ pkg.name }}</span>
          </div>
        </div>
      </div>

      <!-- 貼圖列表 -->
      <div class="sticker-grid" v-loading="loading">
        <div
          v-for="sticker in currentStickers"
          :key="sticker.stickerId"
          class="sticker-item"
          :class="{ selected: selectedStickerId === sticker.stickerId }"
          @click="selectSticker(sticker)"
        >
          <img
            :src="getStickerUrl(sticker.stickerId)"
            :alt="`Sticker ${sticker.stickerId}`"
            @error="handleStickerError($event)"
          />
        </div>
      </div>

      <!-- 選中的貼圖預覽 -->
      <div class="selected-preview" v-if="selectedStickerId">
        <div class="preview-label">已選擇：</div>
        <div class="preview-content">
          <img :src="getStickerUrl(selectedStickerId)" alt="Selected sticker" />
          <div class="preview-info">
            <div>貼圖包 ID：{{ selectedPackageId }}</div>
            <div>貼圖 ID：{{ selectedStickerId }}</div>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :disabled="!selectedStickerId" @click="handleConfirm">
        確定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  currentPackageId: { type: String, default: '' },
  currentStickerId: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue', 'select'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const loading = ref(false)
const selectedPackageId = ref('446')
const selectedStickerId = ref('')

// LINE 官方免費貼圖包完整列表（中英文分類）
// 參考: https://developers.line.biz/en/docs/messaging-api/sticker-list/
const stickerPackages = [
  // Moon, James & Friends 系列（官方確認的 stickerId 範圍）
  { packageId: '446', name: 'Moon & James', nameEn: 'Moon & James', stickers: [1988,1989,1990,1991,1992,1993,1994,1995,1996,1997,1998,1999,2000,2001,2002,2003,2004,2005,2006,2007,2008,2009,2010,2011,2012,2013,2014,2015,2016,2017,2018,2019,2020,2021,2022,2023,2024,2025,2026,2027].map(id => ({ stickerId: String(id) })) },
  { packageId: '789', name: 'Brown & Cony', nameEn: 'Brown & Cony', stickers: [10855,10856,10857,10858,10859,10860,10861,10862,10863,10864,10865,10866,10867,10868,10869,10870,10871,10872,10873,10874,10875,10876,10877,10878].map(id => ({ stickerId: String(id) })) },
  { packageId: '1070', name: '部長', nameEn: 'Boss', stickers: [17839,17840,17841,17842,17843,17844,17845,17846,17847,17848,17849,17850,17851,17852,17853,17854,17855].map(id => ({ stickerId: String(id) })) },
  
  // CHOCO & Friends 系列
  { packageId: '6136', name: 'CHOCO & Friends', nameEn: 'CHOCO & Friends', stickers: [10551376,10551377,10551378,10551379,10551380,10551381,10551382,10551383,10551384,10551385,10551386,10551387,10551388,10551389,10551390,10551391,10551392,10551393,10551394,10551395,10551396,10551397,10551398,10551399].map(id => ({ stickerId: String(id) })) },
  { packageId: '6632', name: 'CHOCO 愛心', nameEn: 'CHOCO Love', stickers: [11825374,11825375,11825376,11825377,11825378,11825379,11825380,11825381,11825382,11825383,11825384,11825385,11825386,11825387,11825388,11825389,11825390,11825391,11825392,11825393,11825394,11825395,11825396,11825397].map(id => ({ stickerId: String(id) })) },
  
  // UNIVERSTAR BT21 系列
  { packageId: '6325', name: 'BT21', nameEn: 'UNIVERSTAR BT21', stickers: [10979904,10979905,10979906,10979907,10979908,10979909,10979910,10979911,10979912,10979913,10979914,10979915,10979916,10979917,10979918,10979919].map(id => ({ stickerId: String(id) })) },
  { packageId: '6359', name: 'BT21 第二彈', nameEn: 'BT21 Vol.2', stickers: [11069848,11069849,11069850,11069851,11069852,11069853,11069854,11069855,11069856,11069857,11069858,11069859,11069860,11069861,11069862,11069863,11069864,11069865,11069866,11069867,11069868,11069869,11069870,11069871].map(id => ({ stickerId: String(id) })) },
  { packageId: '8525', name: 'BT21 第三彈', nameEn: 'BT21 Vol.3', stickers: [16581290,16581291,16581292,16581293,16581294,16581295,16581296,16581297,16581298,16581299,16581300,16581301,16581302,16581303,16581304,16581305,16581306,16581307,16581308,16581309,16581310,16581311,16581312,16581313].map(id => ({ stickerId: String(id) })) },
  
  // Sally 系列
  { packageId: '6362', name: 'Sally', nameEn: 'Sally', stickers: [11087920,11087921,11087922,11087923,11087924,11087925,11087926,11087927,11087928,11087929,11087930,11087931,11087932,11087933,11087934,11087935,11087936,11087937,11087938,11087939,11087940,11087941,11087942,11087943].map(id => ({ stickerId: String(id) })) },
  
  // 其他角色
  { packageId: '6370', name: 'Edward', nameEn: 'Edward', stickers: [11088016,11088017,11088018,11088019,11088020,11088021,11088022,11088023,11088024,11088025,11088026,11088027,11088028,11088029,11088030,11088031,11088032,11088033,11088034,11088035,11088036,11088037,11088038,11088039].map(id => ({ stickerId: String(id) })) },
  
  // LINE Characters 系列
  { packageId: '8515', name: 'LINE 角色', nameEn: 'LINE Characters', stickers: [16581242,16581243,16581244,16581245,16581246,16581247,16581248,16581249,16581250,16581251,16581252,16581253,16581254,16581255,16581256,16581257,16581258,16581259,16581260,16581261,16581262,16581263,16581264,16581265].map(id => ({ stickerId: String(id) })) },
  { packageId: '8522', name: 'Brown & Friends', nameEn: 'Brown & Friends', stickers: [16581266,16581267,16581268,16581269,16581270,16581271,16581272,16581273,16581274,16581275,16581276,16581277,16581278,16581279,16581280,16581281,16581282,16581283,16581284,16581285,16581286,16581287,16581288,16581289].map(id => ({ stickerId: String(id) })) },
  
  // 新版系列
  { packageId: '11537', name: '貼圖小舖', nameEn: 'Sticker Shop', stickers: [52002734,52002735,52002736,52002737,52002738,52002739,52002740,52002741,52002742,52002743,52002744,52002745,52002746,52002747,52002748,52002749,52002750,52002751,52002752,52002753,52002754,52002755,52002756,52002757].map(id => ({ stickerId: String(id) })) },
  { packageId: '11538', name: 'LINE 朋友們', nameEn: 'LINE Friends', stickers: [51626494,51626495,51626496,51626497,51626498,51626499,51626500,51626501,51626502,51626503,51626504,51626505,51626506,51626507,51626508,51626509,51626510,51626511,51626512,51626513,51626514,51626515,51626516,51626517].map(id => ({ stickerId: String(id) })) },
  { packageId: '11539', name: 'LINE 角色 2', nameEn: 'LINE Characters 2', stickers: [52114110,52114111,52114112,52114113,52114114,52114115,52114116,52114117,52114118,52114119,52114120,52114121,52114122,52114123,52114124,52114125,52114126,52114127,52114128,52114129,52114130,52114131,52114132,52114133].map(id => ({ stickerId: String(id) })) }
]

const currentStickers = computed(() => {
  const pkg = stickerPackages.find(p => p.packageId === selectedPackageId.value)
  return pkg ? pkg.stickers : []
})

// 使用 android 路徑而非 iPhone（根據 LINE API 文件）
const getStickerUrl = (stickerId) => {
  return `https://stickershop.line-scdn.net/stickershop/v1/sticker/${stickerId}/android/sticker.png`
}

const selectSticker = (sticker) => {
  selectedStickerId.value = sticker.stickerId
}

const handleStickerError = (event) => {
  event.target.style.opacity = '0.3'
}

const handleClose = () => {
  visible.value = false
}

const handleConfirm = () => {
  if (selectedPackageId.value && selectedStickerId.value) {
    emit('select', {
      packageId: selectedPackageId.value,
      stickerId: selectedStickerId.value
    })
    visible.value = false
  }
}

// 初始化時設定已選擇的貼圖
watch(() => props.modelValue, (val) => {
  if (val) {
    if (props.currentPackageId) {
      selectedPackageId.value = props.currentPackageId
    }
    if (props.currentStickerId) {
      selectedStickerId.value = props.currentStickerId
    }
  }
})

</script>

<style scoped lang="scss">
.sticker-selector {
  .package-tabs {
    margin-bottom: 16px;
    border-bottom: 1px solid #ebeef5;
    padding-bottom: 12px;
    
    .tab-list {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
    }
    
    .tab-item {
      padding: 6px 14px;
      border-radius: 16px;
      cursor: pointer;
      transition: all 0.2s;
      text-align: center;
      background: #f5f7fa;
      border: 1px solid #e4e7ed;
      
      &:hover {
        background: #ecf5ff;
        border-color: #409eff;
      }
      
      &.active {
        background: #409eff;
        color: #fff;
        border-color: #409eff;
      }
      
      .tab-name {
        font-size: 13px;
        font-weight: 500;
      }
    }
  }

  .sticker-grid {
    display: grid;
    grid-template-columns: repeat(6, 1fr);
    gap: 12px;
    max-height: 400px;
    overflow-y: auto;
    padding: 8px;
    background: #f5f7fa;
    border-radius: 8px;
  }

  .sticker-item {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 8px;
    background: #fff;
    border: 2px solid transparent;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      border-color: #409eff;
      transform: scale(1.05);
    }

    &.selected {
      border-color: #409eff;
      background: #ecf5ff;
    }

    img {
      width: 80px;
      height: 80px;
      object-fit: contain;
    }
  }

  .selected-preview {
    margin-top: 16px;
    padding: 12px;
    background: #f0f9eb;
    border-radius: 8px;
    display: flex;
    align-items: center;
    gap: 16px;

    .preview-label {
      font-weight: 500;
      color: #67c23a;
    }

    .preview-content {
      display: flex;
      align-items: center;
      gap: 12px;

      img {
        width: 60px;
        height: 60px;
        object-fit: contain;
      }

      .preview-info {
        font-size: 13px;
        color: #606266;
        line-height: 1.6;
      }
    }
  }
}
</style>
