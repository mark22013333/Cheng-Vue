<template>
  <div>
    <el-tabs type="border-card">
      <el-tab-pane label="秒" v-if="shouldHide('second')">
        <CrontabSecond
          @update="updateCrontabValue"
          :check="checkNumber"
          :cron="crontabValueObj"
          ref="cronsecond"
        />
      </el-tab-pane>

      <el-tab-pane v-if="shouldHide('min')" label="分鐘">
        <CrontabMin
          @update="updateCrontabValue"
          :check="checkNumber"
          :cron="crontabValueObj"
          ref="cronmin"
        />
      </el-tab-pane>

      <el-tab-pane v-if="shouldHide('hour')" label="小時">
        <CrontabHour
          @update="updateCrontabValue"
          :check="checkNumber"
          :cron="crontabValueObj"
          ref="cronhour"
        />
      </el-tab-pane>

      <el-tab-pane label="日" v-if="shouldHide('day')">
        <CrontabDay
          @update="updateCrontabValue"
          :check="checkNumber"
          :cron="crontabValueObj"
          ref="cronday"
        />
      </el-tab-pane>

      <el-tab-pane label="月" v-if="shouldHide('month')">
        <CrontabMonth
          @update="updateCrontabValue"
          :check="checkNumber"
          :cron="crontabValueObj"
          ref="cronmonth"
        />
      </el-tab-pane>

      <el-tab-pane v-if="shouldHide('week')" label="週">
        <CrontabWeek
          @update="updateCrontabValue"
          :check="checkNumber"
          :cron="crontabValueObj"
          ref="cronweek"
        />
      </el-tab-pane>

      <el-tab-pane label="年" v-if="shouldHide('year')">
        <CrontabYear
          @update="updateCrontabValue"
          :check="checkNumber"
          :cron="crontabValueObj"
          ref="cronyear"
        />
      </el-tab-pane>
    </el-tabs>

    <div class="popup-main">
      <!-- 常用時段快捷按鈕 -->
      <div class="common-cron-buttons">
        <p class="title">常用時段</p>
        <br />
        <el-button-group>
          <el-button size="small" @click="setCommonCron('0 0 0 * * ?')">
            <i class="el-icon-moon-night"></i> 每天凌晨12點
          </el-button>
          <el-button size="small" @click="setCommonCron('0 0 2 * * ?')">
            <i class="el-icon-moon"></i> 每天凌晨2點
          </el-button>
          <el-button size="small" @click="setCommonCron('0 0 12 * * ?')">
            <i class="el-icon-sunny"></i> 每天中午12點
          </el-button>
          <el-button size="small" @click="setCommonCron('0 0 18 * * ?')">
            <i class="el-icon-sunset"></i> 每天下午6點
          </el-button>
          <el-button size="small" @click="setCommonCron('0 0 * * * ?')">
            <i class="el-icon-time"></i> 每小時整點
          </el-button>
          <el-button size="small" @click="setCommonCron('0 */30 * * * ?')">
            <i class="el-icon-timer"></i> 每30分鐘
          </el-button>
        </el-button-group>
      </div>

      <div class="popup-result">
        <p class="title">時間表達式</p>
        <table>
          <thead>
            <tr>
              <th v-for="item of tabTitles" width="40" :key="item">{{item}}</th>
              <th>Cron 表達式</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>
                <span>{{crontabValueObj.second}}</span>
              </td>
              <td>
                <span>{{crontabValueObj.min}}</span>
              </td>
              <td>
                <span>{{crontabValueObj.hour}}</span>
              </td>
              <td>
                <span>{{crontabValueObj.day}}</span>
              </td>
              <td>
                <span>{{crontabValueObj.month}}</span>
              </td>
              <td>
                <span>{{crontabValueObj.week}}</span>
              </td>
              <td>
                <span>{{crontabValueObj.year}}</span>
              </td>
              <td>
                <span>{{crontabValueString}}</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <CrontabResult :ex="crontabValueString"></CrontabResult>

      <div class="pop_btn">
        <el-button type="primary" @click="submitFill">確定</el-button>
        <el-button type="warning" @click="clearCron">重置</el-button>
        <el-button @click="hidePopup">取消</el-button>
      </div>
    </div>
  </div>
</template>

<script>
import CrontabSecond from "./second.vue"
import CrontabMin from "./min.vue"
import CrontabHour from "./hour.vue"
import CrontabDay from "./day.vue"
import CrontabMonth from "./month.vue"
import CrontabWeek from "./week.vue"
import CrontabYear from "./year.vue"
import CrontabResult from "./result.vue"

export default {
  data() {
    return {
      tabTitles: ["秒", "分鐘", "小時", "日", "月", "週", "年"],
      tabActive: 0,
      myindex: 0,
      crontabValueObj: {
        second: "0",
        min: "0",
        hour: "0",
        day: "*",
        month: "*",
        week: "?",
        year: "",
      },
    }
  },
  name: "vcrontab",
  props: ["expression", "hideComponent"],
  methods: {
    shouldHide(key) {
      if (this.hideComponent && this.hideComponent.includes(key)) return false
      return true
    },
    resolveExp() {
      // 反解析 表達式
      if (this.expression) {
        let arr = this.expression.split(" ")
        if (arr.length >= 6) {
          //6 位以上是合法表達式
          let obj = {
            second: arr[0],
            min: arr[1],
            hour: arr[2],
            day: arr[3],
            month: arr[4],
            week: arr[5],
            year: arr[6] ? arr[6] : "",
          }
          this.crontabValueObj = {
            ...obj,
          }
          for (let i in obj) {
            if (obj[i]) this.changeRadio(i, obj[i])
          }
        }
      } else {
        // 沒有傳入的表達式 則還原
        this.clearCron()
      }
    },
    // tab切換值
    tabCheck(index) {
      this.tabActive = index
    },
    // 由子元件觸發，更改表達式組成的欄位值
    updateCrontabValue(name, value, from) {
      "updateCrontabValue", name, value, from
      this.crontabValueObj[name] = value
      if (from && from !== name) {
        console.log(`來自元件 ${from} 改變了 ${name} ${value}`)
        this.changeRadio(name, value)
      }
    },
    // 賦值到元件
    changeRadio(name, value) {
      let arr = ["second", "min", "hour", "month"],
        refName = "cron" + name,
        insValue

      if (!this.$refs[refName]) return

      if (arr.includes(name)) {
        if (value === "*") {
          insValue = 1
        } else if (value.indexOf("-") > -1) {
          let indexArr = value.split("-")
          isNaN(indexArr[0])
            ? (this.$refs[refName].cycle01 = 0)
            : (this.$refs[refName].cycle01 = Number(indexArr[0]))
          this.$refs[refName].cycle02 = Number(indexArr[1])
          insValue = 2
        } else if (value.indexOf("/") > -1) {
          let indexArr = value.split("/")
          isNaN(indexArr[0])
            ? (this.$refs[refName].average01 = 0)
            : (this.$refs[refName].average01 = Number(indexArr[0]))
          this.$refs[refName].average02 = Number(indexArr[1])
          insValue = 3
        } else {
          insValue = 4
          this.$refs[refName].checkboxList = value.split(",").map(Number)
        }
      } else if (name == "day") {
        if (value === "*") {
          insValue = 1
        } else if (value == "?") {
          insValue = 2
        } else if (value.indexOf("-") > -1) {
          let indexArr = value.split("-")
          isNaN(indexArr[0])
            ? (this.$refs[refName].cycle01 = 0)
            : (this.$refs[refName].cycle01 = Number(indexArr[0]))
          this.$refs[refName].cycle02 = Number(indexArr[1])
          insValue = 3
        } else if (value.indexOf("/") > -1) {
          let indexArr = value.split("/")
          isNaN(indexArr[0])
            ? (this.$refs[refName].average01 = 0)
            : (this.$refs[refName].average01 = Number(indexArr[0]))
          this.$refs[refName].average02 = Number(indexArr[1])
          insValue = 4
        } else if (value.indexOf("W") > -1) {
          let indexArr = value.split("W")
          isNaN(indexArr[0])
            ? (this.$refs[refName].workday = 0)
            : (this.$refs[refName].workday = Number(indexArr[0]))
          insValue = 5
        } else if (value === "L") {
          insValue = 6
        } else {
          this.$refs[refName].checkboxList = value.split(",").map(Number)
          insValue = 7
        }
      } else if (name == "week") {
        if (value === "*") {
          insValue = 1
        } else if (value == "?") {
          insValue = 2
        } else if (value.indexOf("-") > -1) {
          let indexArr = value.split("-")
          isNaN(indexArr[0])
            ? (this.$refs[refName].cycle01 = 0)
            : (this.$refs[refName].cycle01 = Number(indexArr[0]))
          this.$refs[refName].cycle02 = Number(indexArr[1])
          insValue = 3
        } else if (value.indexOf("#") > -1) {
          let indexArr = value.split("#")
          isNaN(indexArr[0])
            ? (this.$refs[refName].average01 = 1)
            : (this.$refs[refName].average01 = Number(indexArr[0]))
          this.$refs[refName].average02 = Number(indexArr[1])
          insValue = 4
        } else if (value.indexOf("L") > -1) {
          let indexArr = value.split("L")
          isNaN(indexArr[0])
            ? (this.$refs[refName].weekday = 1)
            : (this.$refs[refName].weekday = Number(indexArr[0]))
          insValue = 5
        } else {
          this.$refs[refName].checkboxList = value.split(",").map(Number)
          insValue = 6
        }
      } else if (name == "year") {
        if (value == "") {
          insValue = 1
        } else if (value == "*") {
          insValue = 2
        } else if (value.indexOf("-") > -1) {
          insValue = 3
        } else if (value.indexOf("/") > -1) {
          insValue = 4
        } else {
          this.$refs[refName].checkboxList = value.split(",").map(Number)
          insValue = 5
        }
      }
      this.$refs[refName].radioValue = insValue
    },
    // 表單選項的子元件校驗數字格式（通過-props傳遞）
    checkNumber(value, minLimit, maxLimit) {
      // 檢查必須為整數
      value = Math.floor(value)
      if (value < minLimit) {
        value = minLimit
      } else if (value > maxLimit) {
        value = maxLimit
      }
      return value
    },
    // 隱藏彈窗
    hidePopup() {
      this.$emit("hide")
    },
    // 填充表達式
    submitFill() {
      this.$emit("fill", this.crontabValueString)
      this.hidePopup()
    },
    clearCron() {
      // 還原選擇項（預設為凌晨12點）
      ("準備還原")
      this.crontabValueObj = {
        second: "0",
        min: "0",
        hour: "0",
        day: "*",
        month: "*",
        week: "?",
        year: "",
      }
      for (let j in this.crontabValueObj) {
        this.changeRadio(j, this.crontabValueObj[j])
      }
    },
    // 設定常用 Cron 表達式
    setCommonCron(cronStr) {
      let arr = cronStr.split(" ")
      if (arr.length >= 6) {
        let obj = {
          second: arr[0],
          min: arr[1],
          hour: arr[2],
          day: arr[3],
          month: arr[4],
          week: arr[5],
          year: arr[6] ? arr[6] : "",
        }
        this.crontabValueObj = { ...obj }
        for (let i in obj) {
          if (obj[i]) this.changeRadio(i, obj[i])
        }
      }
    },
  },
  computed: {
    crontabValueString: function() {
      let obj = this.crontabValueObj
      let str =
        obj.second +
        " " +
        obj.min +
        " " +
        obj.hour +
        " " +
        obj.day +
        " " +
        obj.month +
        " " +
        obj.week +
        (obj.year == "" ? "" : " " + obj.year)
      return str
    },
  },
  components: {
    CrontabSecond,
    CrontabMin,
    CrontabHour,
    CrontabDay,
    CrontabMonth,
    CrontabWeek,
    CrontabYear,
    CrontabResult,
  },
  watch: {
    expression: "resolveExp",
    hideComponent(value) {
      // 隱藏部分元件
    },
  },
  mounted: function() {
    this.resolveExp()
  },
}
</script>
<style scoped>
.pop_btn {
  text-align: center;
  margin-top: 20px;
}
.popup-main {
  position: relative;
  margin: 10px auto;
  background: #fff;
  border-radius: 5px;
  font-size: 12px;
  overflow: hidden;
}
.common-cron-buttons {
  box-sizing: border-box;
  margin: 20px auto 10px;
  padding: 15px;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  background: #fafafa;
  position: relative;
}
.common-cron-buttons .title {
  position: absolute;
  top: -12px;
  left: 50%;
  width: 100px;
  font-size: 14px;
  margin-left: -50px;
  text-align: center;
  line-height: 24px;
  background: #fafafa;
  color: #606266;
  font-weight: 500;
}
.common-cron-buttons .el-button-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}
.common-cron-buttons .el-button-group .el-button {
  margin: 0;
  border-radius: 4px;
  min-width: 140px;
}
.common-cron-buttons .el-button-group .el-button:hover {
  color: #409eff;
  border-color: #c6e2ff;
  background-color: #ecf5ff;
}
.common-cron-buttons .el-button-group .el-button i {
  margin-right: 4px;
}
.popup-title {
  overflow: hidden;
  line-height: 34px;
  padding-top: 6px;
  background: #f2f2f2;
}
.popup-result {
  box-sizing: border-box;
  line-height: 24px;
  margin: 25px auto;
  padding: 15px 10px 10px;
  border: 1px solid #ccc;
  position: relative;
}
.popup-result .title {
  position: absolute;
  top: -28px;
  left: 50%;
  width: 140px;
  font-size: 14px;
  margin-left: -70px;
  text-align: center;
  line-height: 30px;
  background: #fff;
}
.popup-result table {
  text-align: center;
  width: 100%;
  margin: 0 auto;
}
.popup-result table span {
  display: block;
  width: 100%;
  font-family: arial;
  line-height: 30px;
  height: 30px;
  white-space: nowrap;
  overflow: hidden;
  border: 1px solid #e8e8e8;
}
.popup-result-scroll {
  font-size: 12px;
  line-height: 24px;
  height: 10em;
  overflow-y: auto;
}
</style>
