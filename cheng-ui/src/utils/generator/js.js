import { titleCase } from '@/utils/index'
import { trigger } from './config'
// 文件大小設定
const units = {
  KB: '1024',
  MB: '1024 / 1024',
  GB: '1024 / 1024 / 1024',
}
/**
 * @name: 產生js需要的資料
 * @description: 產生js需要的資料
 * @param {*} conf
 * @param {*} type 彈窗或表單
 * @return {*}
 */
export function makeUpJs(conf, type) {
  conf = JSON.parse(JSON.stringify(conf))
  const dataList = []
  const ruleList = []
  const optionsList = []
  const propsList = []
  const methodList = []
  const uploadVarList = []

  conf.fields.forEach((el) => {
    buildAttributes(
      el,
      dataList,
      ruleList,
      optionsList,
      methodList,
      propsList,
      uploadVarList
    )
  })

  const script = buildexport(
    conf,
    type,
    dataList.join('\n'),
    ruleList.join('\n'),
    optionsList.join('\n'),
    uploadVarList.join('\n'),
    propsList.join('\n'),
    methodList.join('\n')
  )

  return script
}
/**
 * @name: 產生參數
 * @description: 產生參數，包括表單資料表單驗證資料，多選選項資料，上傳資料等
 * @return {*}
 */
function buildAttributes(
  el,
  dataList,
  ruleList,
  optionsList,
  methodList,
  propsList,
  uploadVarList
){
  buildData(el, dataList)
  buildRules(el, ruleList)

  if (el.options && el.options.length) {
    buildOptions(el, optionsList)
    if (el.dataType === 'dynamic') {
      const model = `${el.vModel}Options`
      const options = titleCase(model)
      buildOptionMethod(`get${options}`, model, methodList)
    }
  }

  if (el.props && el.props.props) {
    buildProps(el, propsList)
  }

  if (el.action && el.tag === 'el-upload') {
    uploadVarList.push(
      `
      // 上傳請求路徑
      const ${el.vModel}Action = ref('${el.action}')
      // 上傳文件列表
      const ${el.vModel}fileList =  ref([])`
    )
    methodList.push(buildBeforeUpload(el))
    if (!el['auto-upload']) {
      methodList.push(buildSubmitUpload(el))
    }
  }

  if (el.children) {
    el.children.forEach((el2) => {
      buildAttributes(
        el2,
        dataList,
        ruleList,
        optionsList,
        methodList,
        propsList,
        uploadVarList
      )
    })
  }
}
/**
 * @name: 產生表單資料formData
 * @description: 產生表單資料formData
 * @param {*} conf
 * @param {*} dataList 資料列表
 * @return {*}
 */
function buildData(conf, dataList) {
  if (conf.vModel === undefined) return
  let defaultValue
  if (typeof conf.defaultValue === 'string' && !conf.multiple) {
    defaultValue = `'${conf.defaultValue}'`
  } else {
    defaultValue = `${JSON.stringify(conf.defaultValue)}`
  }
  dataList.push(`${conf.vModel}: ${defaultValue},`)
}
/**
 * @name: 產生表單驗證資料rule
 * @description: 產生表單驗證資料rule
 * @param {*} conf
 * @param {*} ruleList 驗證資料列表
 * @return {*}
 */
function buildRules(conf, ruleList) {
  if (conf.vModel === undefined) return
  const rules = []
  if (trigger[conf.tag]) {
    if (conf.required) {
      const type = Array.isArray(conf.defaultValue) ? "type: 'array'," : ''
      let message = Array.isArray(conf.defaultValue)
        ? `請至少選擇一個${conf.vModel}`
        : conf.placeholder
      if (message === undefined) message = `${conf.label}不能為空`
      rules.push(
        `{ required: true, ${type} message: '${message}', trigger: '${
          trigger[conf.tag]
        }' }`
      )
    }
    if (conf.regList && Array.isArray(conf.regList)) {
      conf.regList.forEach((item) => {
        if (item.pattern) {
          rules.push(
            `{ pattern: new RegExp(${item.pattern}), message: '${
              item.message
            }', trigger: '${trigger[conf.tag]}' }`
          )
        }
      })
    }
    ruleList.push(`${conf.vModel}: [${rules.join(',')}],`)
  }
}
/**
 * @name: 產生選項資料
 * @description: 產生選項資料，單選多選下拉等
 * @param {*} conf
 * @param {*} optionsList 選項資料列表
 * @return {*}
 */
function buildOptions(conf, optionsList) {
  if (conf.vModel === undefined) return
  if (conf.dataType === 'dynamic') {
    conf.options = []
  }
  const str = `const ${conf.vModel}Options = ref(${JSON.stringify(conf.options)})`
  optionsList.push(str)
}
/**
 * @name: 產生方法
 * @description: 產生方法
 * @param {*} methodName 方法名
 * @param {*} model
 * @param {*} methodList 方法列表
 * @return {*}
 */
function buildOptionMethod(methodName, model, methodList) {
  const str = `function ${methodName}() {
    // TODO 發起請求取得資料
    ${model}.value
  }`
  methodList.push(str)
}
/**
 * @name: 產生表單元件需要的props設定
 * @description: 產生表單元件需要的props設定，如；聯集元件
 * @param {*} conf
 * @param {*} propsList
 * @return {*}
 */
function buildProps(conf, propsList) {
  if (conf.dataType === 'dynamic') {
    conf.valueKey !== 'value' && (conf.props.props.value = conf.valueKey)
    conf.labelKey !== 'label' && (conf.props.props.label = conf.labelKey)
    conf.childrenKey !== 'children' &&
      (conf.props.props.children = conf.childrenKey)
  }
  const str = `
  // props設定
  const ${conf.vModel}Props = ref(${JSON.stringify(conf.props.props)})`
  propsList.push(str)
}
/**
 * @name: 產生上傳元件的相關内容
 * @description: 產生上傳元件的相關内容
 * @param {*} conf
 * @return {*}
 */
function buildBeforeUpload(conf) {
  const unitNum = units[conf.sizeUnit]
  let rightSizeCode = ''
  let acceptCode = ''
  const returnList = []
  if (conf.fileSize) {
    rightSizeCode = `let isRightSize = file.size / ${unitNum} < ${conf.fileSize}
    if(!isRightSize){
      proxy.$modal.msgError('文件大小超過 ${conf.fileSize}${conf.sizeUnit}')
    }`
    returnList.push('isRightSize')
  }
  if (conf.accept) {
    acceptCode = `let isAccept = new RegExp('${conf.accept}').test(file.type)
    if(!isAccept){
      proxy.$modal.msgError('應該選擇${conf.accept}類型的文件')
    }`
    returnList.push('isAccept')
  }
  const str = `
  /**
   * @name: 上傳之前的文件判斷
   * @description: 上傳之前的文件判斷，判斷文件大小文件類型等
   * @param {*} file
   * @return {*}
   */
  function ${conf.vModel}BeforeUpload(file) {
    ${rightSizeCode}
    ${acceptCode}
    return ${returnList.join('&&')}
  }`
  return returnList.length ? str : ''
}
/**
 * @name: 產生提交表單方法
 * @description: 產生提交表單方法
 * @param {Object} conf vModel 表單ref
 * @return {*}
 */
function buildSubmitUpload(conf) {
  const str = `function submitUpload() {
    this.$refs['${conf.vModel}'].submit()
  }`
  return str
}
/**
 * @name: 組裝js代碼
 * @description: 組裝js代碼方法
 * @return {*}
 */
function buildexport(
  conf,
  type,
  data,
  rules,
  selectOptions,
  uploadVar,
  props,
  methods
) {
  let str = `
    const { proxy } = getCurrentInstance()
    const ${conf.formRef} = ref()
    const data = reactive({
      ${conf.formModel}: {
        ${data}
      },
      ${conf.formRules}: {
        ${rules}
      }
    })

    const {${conf.formModel}, ${conf.formRules}} = toRefs(data)

    ${selectOptions}

    ${uploadVar}

    ${props}

    ${methods}
  `

  if(type === 'dialog') {
    str += `
      // 彈窗設定
      const dialogVisible = defineModel()
      // 彈窗確認回調
      const emit = defineEmits(['confirm'])
      /**
       * @name: 彈窗打開後執行
       * @description: 彈窗打開後執行方法
       * @return {*}
       */
      function onOpen(){

      }
      /**
       * @name: 彈窗關閉時執行
       * @description: 彈窗關閉方法，重置表單
       * @return {*}
       */
      function onClose(){
        ${conf.formRef}.value.resetFields()
      }
      /**
       * @name: 彈窗取消
       * @description: 彈窗取消方法
       * @return {*}
       */
      function close(){
        dialogVisible.value = false
      }
      /**
       * @name: 彈窗表單提交
       * @description: 彈窗表單提交方法
       * @return {*}
       */
      function handelConfirm(){
        ${conf.formRef}.value.validate((valid) => {
          if (!valid) return
          // TODO 提交表單

          close()
          // 回調父級元件
          emit('confirm')
        })
      }
    `
  } else {
    str += `
    /**
     * @name: 表單提交
     * @description: 表單提交方法
     * @return {*}
     */
    function submitForm() {
      ${conf.formRef}.value.validate((valid) => {
        if (!valid) return
        // TODO 提交表單
      })
    }
    /**
     * @name: 表單重置
     * @description: 表單重置方法
     * @return {*}
     */
    function resetForm() {
      ${conf.formRef}.value.resetFields()
    }
    `
  }
  return str
}
