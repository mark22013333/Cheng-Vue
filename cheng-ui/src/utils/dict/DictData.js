/**
 * @classdesc 字典數據
 * @property {String} label 標籤
 * @property {*} value 標籤
 * @property {Object} raw 原始數據
 */
export default class DictData {
  constructor(label, value, raw) {
    this.label = label
    this.value = value
    this.raw = raw
  }
}
