<template>
	<el-form size="small">
		<el-form-item>
			<el-radio v-model='radioValue' :label="1">
        日，允許的通用符號[, - * ? / L W]
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="2">
				不指定
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="3">
        週期從
				<el-input-number v-model='cycle01' :min="1" :max="30" /> -
				<el-input-number v-model='cycle02' :min="cycle01 ? cycle01 + 1 : 2" :max="31" /> 日
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="4">
        從
        <el-input-number v-model='average01' :max="30" :min="1"/>
        號開始，每
        <el-input-number v-model='average02' :max="31 - average01 || 1" :min="1"/>
        日執行一次
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="5">
				每月
        <el-input-number v-model='workday' :max="31" :min="1"/>
        號最近的那個工作日
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="6">
        本月最後一天
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="7">
				指定
        <el-select v-model="checkboxList" clearable multiple placeholder="可多選" style="width:100%">
					<el-option v-for="item in 31" :key="item" :value="item">{{item}}</el-option>
				</el-select>
			</el-radio>
		</el-form-item>
	</el-form>
</template>

<script>
export default {
	data() {
		return {
			radioValue: 1,
			workday: 1,
			cycle01: 1,
			cycle02: 2,
			average01: 1,
			average02: 1,
			checkboxList: [],
			checkNum: this.$options.propsData.check
		}
	},
	name: 'crontab-day',
	props: ['check', 'cron'],
	methods: {
    // 單選按鈕值變化時
		radioChange() {
			('day rachange')
			if (this.radioValue !== 2 && this.cron.week !== '?') {
				this.$emit('update', 'week', '?', 'day')
			}

			switch (this.radioValue) {
				case 1:
					this.$emit('update', 'day', '*')
					break
				case 2:
					this.$emit('update', 'day', '?')
					break
				case 3:
					this.$emit('update', 'day', this.cycleTotal)
					break
				case 4:
					this.$emit('update', 'day', this.averageTotal)
					break
				case 5:
					this.$emit('update', 'day', this.workday + 'W')
					break
				case 6:
					this.$emit('update', 'day', 'L')
					break
				case 7:
					this.$emit('update', 'day', this.checkboxString)
					break
			}
			('day rachange end')
		},
    // 週期兩個值變化時
		cycleChange() {
			if (this.radioValue == '3') {
				this.$emit('update', 'day', this.cycleTotal)
			}
		},
    // 平均兩個值變化時
		averageChange() {
			if (this.radioValue == '4') {
				this.$emit('update', 'day', this.averageTotal)
			}
		},
    // 最近工作日值變化時
		workdayChange() {
			if (this.radioValue == '5') {
				this.$emit('update', 'day', this.workdayCheck + 'W')
			}
		},
    // checkbox值變化時
		checkboxChange() {
			if (this.radioValue == '7') {
				this.$emit('update', 'day', this.checkboxString)
			}
		}
	},
	watch: {
		'radioValue': 'radioChange',
		'cycleTotal': 'cycleChange',
		'averageTotal': 'averageChange',
		'workdayCheck': 'workdayChange',
		'checkboxString': 'checkboxChange',
	},
	computed: {
    // 計算兩個週期值
		cycleTotal: function () {
			const cycle01 = this.checkNum(this.cycle01, 1, 30)
			const cycle02 = this.checkNum(this.cycle02, cycle01 ? cycle01 + 1 : 2, 31, 31)
			return cycle01 + '-' + cycle02
		},
    // 計算平均用到的值
		averageTotal: function () {
			const average01 = this.checkNum(this.average01, 1, 30)
			const average02 = this.checkNum(this.average02, 1, 31 - average01 || 0)
			return average01 + '/' + average02
		},
    // 計算工作日格式
		workdayCheck: function () {
			const workday = this.checkNum(this.workday, 1, 31)
			return workday
		},
    // 計算勾選的checkbox值合集
		checkboxString: function () {
			let str = this.checkboxList.join()
			return str == '' ? '*' : str
		}
	}
}
</script>
