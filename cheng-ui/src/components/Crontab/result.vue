<template>
	<div class="popup-result">
    <p class="title">最近5次執行時間</p>
		<ul class="popup-result-scroll">
			<template v-if='isShow'>
				<li v-for='item in resultList' :key="item">{{item}}</li>
			</template>
      <li v-else>計算結果中...</li>
		</ul>
	</div>
</template>

<script>
export default {
	data() {
		return {
			dayRule: '',
			dayRuleSup: '',
			dateArr: [],
			resultList: [],
			isShow: false
		}
	},
	name: 'crontab-result',
	methods: {
    // 表達式值變化時，開始去計算結果
		expressionChange() {

      // 計算開始-隱藏結果
			this.isShow = false
      // 取得規則陣列[0秒、1分、2時、3日、4月、5星期、6年]
			let ruleArr = this.ex.split(' ')
      // 用於記錄進入循環的次數
			let nums = 0
      // 用於暫時存符號時間規則結果的陣列
			let resultArr = []
      // 取得目前時間精確至[年、月、日、時、分、秒]
			let nTime = new Date()
			let nYear = nTime.getFullYear()
			let nMonth = nTime.getMonth() + 1
			let nDay = nTime.getDate()
			let nHour = nTime.getHours()
			let nMin = nTime.getMinutes()
			let nSecond = nTime.getSeconds()
      // 根據規則取得到近100年可能年陣列、月陣列等等
			this.getSecondArr(ruleArr[0])
			this.getMinArr(ruleArr[1])
			this.getHourArr(ruleArr[2])
			this.getDayArr(ruleArr[3])
			this.getMonthArr(ruleArr[4])
			this.getWeekArr(ruleArr[5])
			this.getYearArr(ruleArr[6], nYear)
      // 將取得到的陣列賦值-方便使用
			let sDate = this.dateArr[0]
			let mDate = this.dateArr[1]
			let hDate = this.dateArr[2]
			let DDate = this.dateArr[3]
			let MDate = this.dateArr[4]
			let YDate = this.dateArr[5]
      // 取得目前時間在陣列中的索引
			let sIdx = this.getIndex(sDate, nSecond)
			let mIdx = this.getIndex(mDate, nMin)
			let hIdx = this.getIndex(hDate, nHour)
			let DIdx = this.getIndex(DDate, nDay)
			let MIdx = this.getIndex(MDate, nMonth)
			let YIdx = this.getIndex(YDate, nYear)
      // 重置月日時分秒的函數(後面用的比較多)
			const resetSecond = function () {
				sIdx = 0
				nSecond = sDate[sIdx]
			}
			const resetMin = function () {
				mIdx = 0
				nMin = mDate[mIdx]
				resetSecond()
			}
			const resetHour = function () {
				hIdx = 0
				nHour = hDate[hIdx]
				resetMin()
			}
			const resetDay = function () {
				DIdx = 0
				nDay = DDate[DIdx]
				resetHour()
			}
			const resetMonth = function () {
				MIdx = 0
				nMonth = MDate[MIdx]
				resetDay()
			}
      // 如果目前年份不為陣列中目前值
			if (nYear !== YDate[YIdx]) {
				resetMonth()
			}
      // 如果目前月份不為陣列中目前值
			if (nMonth !== MDate[MIdx]) {
				resetDay()
			}
      // 如果目前“日”不為陣列中目前值
			if (nDay !== DDate[DIdx]) {
				resetHour()
			}
      // 如果目前“時”不為陣列中目前值
			if (nHour !== hDate[hIdx]) {
				resetMin()
			}
      // 如果目前“分”不為陣列中目前值
			if (nMin !== mDate[mIdx]) {
				resetSecond()
			}

      // 循環年份陣列
			goYear: for (let Yi = YIdx; Yi < YDate.length; Yi++) {
				let YY = YDate[Yi]
        // 如果到達最大值時
				if (nMonth > MDate[MDate.length - 1]) {
					resetMonth()
					continue
				}
        // 循環月份陣列
				goMonth: for (let Mi = MIdx; Mi < MDate.length; Mi++) {
          // 賦值、方便後面運算
					let MM = MDate[Mi];
					MM = MM < 10 ? '0' + MM : MM
          // 如果到達最大值時
					if (nDay > DDate[DDate.length - 1]) {
						resetDay()
						if (Mi == MDate.length - 1) {
							resetMonth()
							continue goYear
						}
						continue
					}
          // 循環日期陣列
					goDay: for (let Di = DIdx; Di < DDate.length; Di++) {
            // 賦值、方便後面運算
						let DD = DDate[Di]
						let thisDD = DD < 10 ? '0' + DD : DD

            // 如果到達最大值時
						if (nHour > hDate[hDate.length - 1]) {
							resetHour()
							if (Di == DDate.length - 1) {
								resetDay()
								if (Mi == MDate.length - 1) {
									resetMonth()
									continue goYear
								}
								continue goMonth
							}
							continue
						}

            // 判斷日期的合法性，不合法的話也是跳出目前循環
						if (this.checkDate(YY + '-' + MM + '-' + thisDD + ' 00:00:00') !== true && this.dayRule !== 'workDay' && this.dayRule !== 'lastWeek' && this.dayRule !== 'lastDay') {
							resetDay()
							continue goMonth
						}
            // 如果日期規則中有值時
						if (this.dayRule == 'lastDay') {
              // 如果不是合法日期則需要將前將日期調到合法日期即月末最後一天

							if (this.checkDate(YY + '-' + MM + '-' + thisDD + ' 00:00:00') !== true) {
								while (DD > 0 && this.checkDate(YY + '-' + MM + '-' + thisDD + ' 00:00:00') !== true) {
									DD--
									thisDD = DD < 10 ? '0' + DD : DD
								}
							}
						} else if (this.dayRule == 'workDay') {
              // 校驗並調整如果是2月30號這種日期傳進來時需調整至正常月底
							if (this.checkDate(YY + '-' + MM + '-' + thisDD + ' 00:00:00') !== true) {
								while (DD > 0 && this.checkDate(YY + '-' + MM + '-' + thisDD + ' 00:00:00') !== true) {
									DD--
									thisDD = DD < 10 ? '0' + DD : DD
								}
							}
              // 取得達到條件的日期是星期X
							let thisWeek = this.formatDate(new Date(YY + '-' + MM + '-' + thisDD + ' 00:00:00'), 'week')
              // 當星期日時
							if (thisWeek == 1) {
                // 先找下一個日，並判斷是否為月底
								DD++
								thisDD = DD < 10 ? '0' + DD : DD
                // 判斷下一日已經不是合法日期
								if (this.checkDate(YY + '-' + MM + '-' + thisDD + ' 00:00:00') !== true) {
									DD -= 3
								}
							} else if (thisWeek == 7) {
                // 當星期6時只需判斷不是1號就可進行操作
								if (this.dayRuleSup !== 1) {
									DD--
								} else {
									DD += 2
								}
							}
						} else if (this.dayRule == 'weekDay') {
              // 如果指定了是星期幾
              // 取得目前日期是屬於星期幾
							let thisWeek = this.formatDate(new Date(YY + '-' + MM + '-' + DD + ' 00:00:00'), 'week')
              // 校驗目前星期是否在星期池（dayRuleSup）中
							if (this.dayRuleSup.indexOf(thisWeek) < 0) {
                // 如果到達最大值時
								if (Di == DDate.length - 1) {
									resetDay()
									if (Mi == MDate.length - 1) {
										resetMonth()
										continue goYear
									}
									continue goMonth
								}
								continue
							}
						} else if (this.dayRule == 'assWeek') {
              // 如果指定了是第幾週的星期幾
              // 取得每月1號是屬於星期幾
							let thisWeek = this.formatDate(new Date(YY + '-' + MM + '-' + DD + ' 00:00:00'), 'week')
							if (this.dayRuleSup[1] >= thisWeek) {
								DD = (this.dayRuleSup[0] - 1) * 7 + this.dayRuleSup[1] - thisWeek + 1
							} else {
								DD = this.dayRuleSup[0] * 7 + this.dayRuleSup[1] - thisWeek + 1
							}
						} else if (this.dayRule == 'lastWeek') {
              // 如果指定了每月最後一個星期幾
              // 校驗並調整如果是2月30號這種日期傳進來時需調整至正常月底
							if (this.checkDate(YY + '-' + MM + '-' + thisDD + ' 00:00:00') !== true) {
								while (DD > 0 && this.checkDate(YY + '-' + MM + '-' + thisDD + ' 00:00:00') !== true) {
									DD--
									thisDD = DD < 10 ? '0' + DD : DD
								}
							}
              // 取得月末最後一天是星期幾
							let thisWeek = this.formatDate(new Date(YY + '-' + MM + '-' + thisDD + ' 00:00:00'), 'week')
              // 找到要求中最近的那個星期幾
							if (this.dayRuleSup < thisWeek) {
								DD -= thisWeek - this.dayRuleSup
							} else if (this.dayRuleSup > thisWeek) {
								DD -= 7 - (this.dayRuleSup - thisWeek)
							}
						}
            // 判斷時間值是否小於10替換成“05”這種格式
						DD = DD < 10 ? '0' + DD : DD

            // 循環“時”陣列
						goHour: for (let hi = hIdx; hi < hDate.length; hi++) {
							let hh = hDate[hi] < 10 ? '0' + hDate[hi] : hDate[hi]

              // 如果到達最大值時
							if (nMin > mDate[mDate.length - 1]) {
								resetMin()
								if (hi == hDate.length - 1) {
									resetHour()
									if (Di == DDate.length - 1) {
										resetDay()
										if (Mi == MDate.length - 1) {
											resetMonth()
											continue goYear
										}
										continue goMonth
									}
									continue goDay
								}
								continue
							}
              // 循環"分"陣列
							goMin: for (let mi = mIdx; mi < mDate.length; mi++) {
								let mm = mDate[mi] < 10 ? '0' + mDate[mi] : mDate[mi]

                // 如果到達最大值時
								if (nSecond > sDate[sDate.length - 1]) {
									resetSecond()
									if (mi == mDate.length - 1) {
										resetMin()
										if (hi == hDate.length - 1) {
											resetHour()
											if (Di == DDate.length - 1) {
												resetDay()
												if (Mi == MDate.length - 1) {
													resetMonth()
													continue goYear
												}
												continue goMonth
											}
											continue goDay
										}
										continue goHour
									}
									continue
								}
                // 循環"秒"陣列
								goSecond: for (let si = sIdx; si <= sDate.length - 1; si++) {
									let ss = sDate[si] < 10 ? '0' + sDate[si] : sDate[si]
                  // 新增目前時間（時間合法性在日期循環時已經判斷）
									if (MM !== '00' && DD !== '00') {
										resultArr.push(YY + '-' + MM + '-' + DD + ' ' + hh + ':' + mm + ':' + ss)
										nums++
									}
                  // 如果則數滿了就登出循環
									if (nums == 5) break goYear
                  // 如果到達最大值時
									if (si == sDate.length - 1) {
										resetSecond()
										if (mi == mDate.length - 1) {
											resetMin()
											if (hi == hDate.length - 1) {
												resetHour()
												if (Di == DDate.length - 1) {
													resetDay()
													if (Mi == MDate.length - 1) {
														resetMonth()
														continue goYear
													}
													continue goMonth
												}
												continue goDay
											}
											continue goHour
										}
										continue goMin
									}
								} //goSecond
							} //goMin
						}//goHour
					}//goDay
				}//goMonth
			}
      // 判斷100年内的結果則數
			if (resultArr.length == 0) {
        this.resultList = ['沒有達到條件的結果！']
			} else {
				this.resultList = resultArr
				if (resultArr.length !== 5) {
          this.resultList.push('最近100年内只有上面' + resultArr.length + '條結果！')
				}
			}
      // 計算完成-顯示結果
			this.isShow = true


		},
    // 用於計算某位數字在陣列中的索引
		getIndex(arr, value) {
			if (value <= arr[0] || value > arr[arr.length - 1]) {
				return 0
			} else {
				for (let i = 0; i < arr.length - 1; i++) {
					if (value > arr[i] && value <= arr[i + 1]) {
						return i + 1
					}
				}
			}
		},
    // 取得"年"陣列
		getYearArr(rule, year) {
			this.dateArr[5] = this.getOrderArr(year, year + 100)
			if (rule !== undefined) {
				if (rule.indexOf('-') >= 0) {
					this.dateArr[5] = this.getCycleArr(rule, year + 100, false)
				} else if (rule.indexOf('/') >= 0) {
					this.dateArr[5] = this.getAverageArr(rule, year + 100)
				} else if (rule !== '*') {
					this.dateArr[5] = this.getAssignArr(rule)
				}
			}
		},
    // 取得"月"陣列
		getMonthArr(rule) {
			this.dateArr[4] = this.getOrderArr(1, 12)
			if (rule.indexOf('-') >= 0) {
				this.dateArr[4] = this.getCycleArr(rule, 12, false)
			} else if (rule.indexOf('/') >= 0) {
				this.dateArr[4] = this.getAverageArr(rule, 12)
			} else if (rule !== '*') {
				this.dateArr[4] = this.getAssignArr(rule)
			}
		},
    // 取得"日"陣列-主要為日期規則
		getWeekArr(rule) {
      // 只有當日期規則的兩個值均為“”時則表達日期是有選項的
			if (this.dayRule == '' && this.dayRuleSup == '') {
				if (rule.indexOf('-') >= 0) {
					this.dayRule = 'weekDay'
					this.dayRuleSup = this.getCycleArr(rule, 7, false)
				} else if (rule.indexOf('#') >= 0) {
					this.dayRule = 'assWeek'
					let matchRule = rule.match(/[0-9]{1}/g)
					this.dayRuleSup = [Number(matchRule[1]), Number(matchRule[0])]
					this.dateArr[3] = [1]
					if (this.dayRuleSup[1] == 7) {
						this.dayRuleSup[1] = 0
					}
				} else if (rule.indexOf('L') >= 0) {
					this.dayRule = 'lastWeek'
					this.dayRuleSup = Number(rule.match(/[0-9]{1,2}/g)[0])
					this.dateArr[3] = [31]
					if (this.dayRuleSup == 7) {
						this.dayRuleSup = 0
					}
				} else if (rule !== '*' && rule !== '?') {
					this.dayRule = 'weekDay'
					this.dayRuleSup = this.getAssignArr(rule)
				}
			}
		},
    // 取得"日"陣列-少量為日期規則
		getDayArr(rule) {
			this.dateArr[3] = this.getOrderArr(1, 31)
			this.dayRule = ''
			this.dayRuleSup = ''
			if (rule.indexOf('-') >= 0) {
				this.dateArr[3] = this.getCycleArr(rule, 31, false)
				this.dayRuleSup = 'null'
			} else if (rule.indexOf('/') >= 0) {
				this.dateArr[3] = this.getAverageArr(rule, 31)
				this.dayRuleSup = 'null'
			} else if (rule.indexOf('W') >= 0) {
				this.dayRule = 'workDay'
				this.dayRuleSup = Number(rule.match(/[0-9]{1,2}/g)[0])
				this.dateArr[3] = [this.dayRuleSup]
			} else if (rule.indexOf('L') >= 0) {
				this.dayRule = 'lastDay'
				this.dayRuleSup = 'null'
				this.dateArr[3] = [31]
			} else if (rule !== '*' && rule !== '?') {
				this.dateArr[3] = this.getAssignArr(rule)
				this.dayRuleSup = 'null'
			} else if (rule == '*') {
				this.dayRuleSup = 'null'
			}
		},
    // 取得"時"陣列
		getHourArr(rule) {
			this.dateArr[2] = this.getOrderArr(0, 23)
			if (rule.indexOf('-') >= 0) {
				this.dateArr[2] = this.getCycleArr(rule, 24, true)
			} else if (rule.indexOf('/') >= 0) {
				this.dateArr[2] = this.getAverageArr(rule, 23)
			} else if (rule !== '*') {
				this.dateArr[2] = this.getAssignArr(rule)
			}
		},
    // 取得"分"陣列
		getMinArr(rule) {
			this.dateArr[1] = this.getOrderArr(0, 59)
			if (rule.indexOf('-') >= 0) {
				this.dateArr[1] = this.getCycleArr(rule, 60, true)
			} else if (rule.indexOf('/') >= 0) {
				// 分的範圍是 0-59，所以 limit 應該是 60
				this.dateArr[1] = this.getAverageArr(rule, 60)
			} else if (rule !== '*') {
				this.dateArr[1] = this.getAssignArr(rule)
			}
		},
    // 取得"秒"陣列
		getSecondArr(rule) {
			this.dateArr[0] = this.getOrderArr(0, 59)
			if (rule.indexOf('-') >= 0) {
				this.dateArr[0] = this.getCycleArr(rule, 60, true)
			} else if (rule.indexOf('/') >= 0) {
				// 秒的範圍是 0-59，所以 limit 應該是 60
				this.dateArr[0] = this.getAverageArr(rule, 60)
			} else if (rule !== '*') {
				this.dateArr[0] = this.getAssignArr(rule)
			}
		},
    // 根據傳進來的min-max返回一個順序的陣列
		getOrderArr(min, max) {
			let arr = []
			for (let i = min; i <= max; i++) {
				arr.push(i)
			}
			return arr
		},
    // 根據規則中指定的零散值返回一個陣列
		getAssignArr(rule) {
			let arr = []
			let assiginArr = rule.split(',')
			for (let i = 0; i < assiginArr.length; i++) {
				arr[i] = Number(assiginArr[i])
			}
			arr.sort(this.compare)
			return arr
		},
    // 根據一定算術規則計算返回一個陣列
		getAverageArr(rule, limit) {
			let arr = []
			let agArr = rule.split('/')
			let min = Number(agArr[0])
			let step = Number(agArr[1])
			// limit 是最大值+1（如秒是60，月是13），所以條件改為 <
			while (min < limit) {
				arr.push(min)
				min += step
			}
			return arr
		},
    // 根據規則返回一個具有週期性的陣列
		getCycleArr(rule, limit, status) {
      // status--表示是否從0開始（true: 0開始，false: 1開始）
			let arr = []
			let cycleArr = rule.split('-')
			let min = Number(cycleArr[0])
			let max = Number(cycleArr[1])
			
			// 處理跨界範圍（如：10-2 表示 10,11,12,1,2）
			if (min > max) {
				// 從 min 到 limit-1（或 limit）
				for (let i = min; i < limit || (status === false && i <= limit); i++) {
					if (status === false && i === 0) continue // 月份不包含 0
					arr.push(i)
				}
				// 從 0（或 1）到 max
				for (let i = (status === false ? 1 : 0); i <= max; i++) {
					arr.push(i)
				}
			} else {
				// 正常範圍
				for (let i = min; i <= max; i++) {
					arr.push(i)
				}
			}
			
			// 不需要排序，因為已經按順序添加
			return arr
		},
    // 比較數字大小（用於Array.sort）
		compare(value1, value2) {
			if (value2 - value1 > 0) {
				return -1
			} else {
				return 1
			}
		},
		// 格式化日期格式如：2017-9-19 18:04:33
		formatDate(value, type) {
      // 計算日期相關值
			let time = typeof value == 'number' ? new Date(value) : value
			let Y = time.getFullYear()
			let M = time.getMonth() + 1
			let D = time.getDate()
			let h = time.getHours()
			let m = time.getMinutes()
			let s = time.getSeconds()
			let week = time.getDay()
      // 如果傳遞了type的話
			if (type == undefined) {
				return Y + '-' + (M < 10 ? '0' + M : M) + '-' + (D < 10 ? '0' + D : D) + ' ' + (h < 10 ? '0' + h : h) + ':' + (m < 10 ? '0' + m : m) + ':' + (s < 10 ? '0' + s : s)
			} else if (type == 'week') {
        // 在quartz中 1為星期日
				return week + 1
			}
		},
    // 檢查日期是否存在
		checkDate(value) {
			let time = new Date(value)
			let format = this.formatDate(time)
			return value === format
		}
	},
	watch: {
		'ex': 'expressionChange'
	},
	props: ['ex'],
	mounted: function () {
    // 初始化 取得一次結果
		this.expressionChange()
	}
}

</script>
