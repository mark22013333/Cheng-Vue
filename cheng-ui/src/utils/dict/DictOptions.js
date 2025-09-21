import {mergeRecursive} from "@/utils/cheng"
import dictConverter from './DictConverter'

export const options = {
  metas: {
    '*': {
      /**
       * 字典請求，方法籤名為function(dictMeta: DictMeta): Promise
       */
      request: (dictMeta) => {
        console.log(`load dict ${dictMeta.type}`)
        return Promise.resolve([])
      },
      /**
       * 字典響應數據轉換器，方法籤名為function(response: Object, dictMeta: DictMeta): DictData
       */
      responseConverter,
      labelField: 'label',
      valueField: 'value',
    },
  },
  /**
   * 預設標籤欄位
   */
  DEFAULT_LABEL_FIELDS: ['label', 'name', 'title'],
  /**
   * 預設值欄位
   */
  DEFAULT_VALUE_FIELDS: ['value', 'id', 'uid', 'key'],
}

/**
 * 映射字典
 * @param {Object} response 字典數據
 * @param {DictMeta} dictMeta 字典元數據
 * @returns {DictData}
 */
function responseConverter(response, dictMeta) {
  const dicts = response.content instanceof Array ? response.content : response
  if (dicts === undefined) {
    console.warn(`no dict data of "${dictMeta.type}" found in the response`)
    return []
  }
  return dicts.map(d => dictConverter(d, dictMeta))
}

export function mergeOptions(src) {
  mergeRecursive(options, src)
}

export default options
