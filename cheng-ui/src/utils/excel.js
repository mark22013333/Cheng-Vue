import * as XLSX from 'xlsx'

/**
 * 解析 Excel 檔案（單一 Sheet）
 * @param {File} file - Excel 檔案
 * @returns {Promise<Array>} 解析後的資料陣列
 */
export function parseExcel(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      try {
        const data = e.target.result
        const workbook = XLSX.read(data, { type: 'array' })
        const firstSheetName = workbook.SheetNames[0]
        const worksheet = workbook.Sheets[firstSheetName]
        const jsonData = XLSX.utils.sheet_to_json(worksheet, { header: 1 })
        resolve(jsonData)
      } catch (error) {
        reject(error)
      }
    }
    reader.onerror = (error) => reject(error)
    reader.readAsArrayBuffer(file)
  })
}

/**
 * 解析 Excel 檔案（所有 Sheet）
 * @param {File} file - Excel 檔案
 * @returns {Promise<Array>} 合併所有 Sheet 的資料陣列（不含標題行）
 */
export function parseExcelAllSheets(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      try {
        const data = e.target.result
        const workbook = XLSX.read(data, { type: 'array' })
        const allData = []
        
        workbook.SheetNames.forEach((sheetName, index) => {
          const worksheet = workbook.Sheets[sheetName]
          const jsonData = XLSX.utils.sheet_to_json(worksheet, { header: 1 })
          // 第一個 Sheet 保留標題行，其他 Sheet 跳過標題行
          if (index === 0) {
            allData.push(...jsonData)
          } else if (jsonData.length > 1) {
            allData.push(...jsonData.slice(1))
          }
        })
        
        resolve(allData)
      } catch (error) {
        reject(error)
      }
    }
    reader.onerror = (error) => reject(error)
    reader.readAsArrayBuffer(file)
  })
}

/**
 * 判斷是否為標題行
 * @param {string} value - 第一欄的值
 * @param {Array} headerKeywords - 標題關鍵字列表
 * @returns {boolean} 是否為標題行
 */
function isHeaderRow(value, headerKeywords = []) {
  if (!value) return false
  const str = value.toString().trim().toLowerCase()
  // 預設的標題關鍵字
  const defaultKeywords = ['line', '使用者', 'user', 'id', '物品', 'item', '編碼', 'code', '標籤', 'tag']
  const allKeywords = [...defaultKeywords, ...headerKeywords]
  return allKeywords.some(keyword => str.includes(keyword.toLowerCase()))
}

/**
 * 通用批次貼標 Excel 解析（支援多 Sheet、額外標籤欄位）
 * @param {File} file - Excel 檔案
 * @param {Object} options - 選項
 * @param {string} options.idField - ID 欄位名稱（用於返回結果）
 * @param {Array} options.headerKeywords - 額外的標題關鍵字
 * @returns {Promise<Array>} 解析後的記錄陣列 [{[idField]: value, extraTagName}]
 */
export function parseBatchTagExcel(file, options = {}) {
  const { idField = 'id', headerKeywords = [] } = options
  
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      try {
        const data = e.target.result
        const workbook = XLSX.read(data, { type: 'array' })
        const records = []
        
        workbook.SheetNames.forEach((sheetName) => {
          const worksheet = workbook.Sheets[sheetName]
          const jsonData = XLSX.utils.sheet_to_json(worksheet, { header: 1 })
          
          if (jsonData.length === 0) return
          
          // 判斷第一行是否為標題行
          const firstRowValue = jsonData[0]?.[0]
          const startIndex = isHeaderRow(firstRowValue, headerKeywords) ? 1 : 0
          
          for (let i = startIndex; i < jsonData.length; i++) {
            const row = jsonData[i]
            const idValue = row[0]?.toString().trim()
            const extraTagName = row[1]?.toString().trim() || null
            
            if (idValue && idValue.length > 0) {
              records.push({ [idField]: idValue, extraTagName })
            }
          }
        })
        
        resolve(records)
      } catch (error) {
        reject(error)
      }
    }
    reader.onerror = (error) => reject(error)
    reader.readAsArrayBuffer(file)
  })
}

/**
 * 解析使用者貼標 Excel（支援多 Sheet、額外標籤欄位）
 * @param {File} file - Excel 檔案
 * @returns {Promise<Array>} 解析後的記錄陣列 [{lineUserId, extraTagName}]
 */
export function parseUserTagExcel(file) {
  return parseBatchTagExcel(file, { 
    idField: 'lineUserId', 
    headerKeywords: ['line', '使用者'] 
  })
}

/**
 * 解析物品貼標 Excel（支援多 Sheet、額外標籤欄位）
 * @param {File} file - Excel 檔案
 * @returns {Promise<Array>} 解析後的記錄陣列 [{itemCode, extraTagName}]
 */
export function parseItemTagExcel(file) {
  return parseBatchTagExcel(file, { 
    idField: 'itemCode', 
    headerKeywords: ['物品', '編碼', 'item', 'code'] 
  })
}

/**
 * 匯出 Excel 範本
 * @param {Array} headers - 標題列
 * @param {Array} sampleData - 範例資料
 * @param {string} filename - 檔案名稱
 */
export function exportTemplate(headers, sampleData, filename) {
  const ws = XLSX.utils.aoa_to_sheet([headers, ...sampleData])
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, 'Sheet1')
  XLSX.writeFile(wb, `${filename}.xlsx`)
}

/**
 * 匯出資料為 Excel
 * @param {Array} data - 資料陣列
 * @param {Array} headers - 標題列
 * @param {string} filename - 檔案名稱
 */
export function exportExcel(data, headers, filename) {
  const ws = XLSX.utils.json_to_sheet(data, { header: headers })
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, 'Sheet1')
  XLSX.writeFile(wb, `${filename}.xlsx`)
}
