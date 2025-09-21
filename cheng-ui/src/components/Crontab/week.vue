<template>
	<el-form size='small'>
		<el-form-item>
			<el-radio v-model='radioValue' :label="1">
        週，允許的通用符號[, - * ? / L #]
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="2">
				不指定
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="3">
        週期從星期
				<el-select clearable v-model="cycle01">
					<el-option
						v-for="(item,index) of weekList"
						:key="index"
						:label="item.value"
						:value="item.key"
						:disabled="item.key === 1"
					>{{item.value}}</el-option>
				</el-select>
				-
				<el-select clearable v-model="cycle02">
					<el-option
						v-for="(item,index) of weekList"
						:key="index"
						:label="item.value"
						:value="item.key"
						:disabled="item.key < cycle01 && item.key !== 1"
					>{{item.value}}</el-option>
				</el-select>
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="4">
				第
        <el-input-number v-model='average01' :max="4" :min="1"/>
        週的星期
				<el-select clearable v-model="average02">
					<el-option v-for="(item,index) of weekList" :key="index" :label="item.value" :value="item.key">{{item.value}}</el-option>
				</el-select>
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="5">
        本月最後一個星期
				<el-select clearable v-model="weekday">
					<el-option v-for="(item,index) of weekList" :key="index" :label="item.value" :value="item.key">{{item.value}}</el-option>
				</el-select>
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="6">
				指定
        <el-select v-model="checkboxList" clearable multiple placeholder="可多選" style="width:100%">
					<el-option v-for="(item,index) of weekList" :key="index" :label="item.value" :value="String(item.key)">{{item.value}}</el-option>
				</el-select>
			</el-radio>
		</el-form-item>

	</el-form>
</template>

<script>
export default {
	data() {
		return {
			radioValue: 2,
			weekday: 2,
			cycle01: 2,
			cycle02: 3,
			average01: 1,
			average02: 2,
			checkboxList: [],
			weekList: [
				{
					key: 2,
					value: '星期一'
				},
				{
					key: 3,
					value: '星期二'
				},
				{
					key: 4,
					value: '星期三'
				},
				{
					key: 5,
					value: '星期四'
				},
				{
					key: 6,
					value: '星期五'
				},
				{
					key: 7,
					value: '星期六'
				},
				{
					key: 1,
					value: '星期日'
				}
			],
			checkNum: this.$options.propsData.check
		}
	},
	name: 'crontab-week',
	props: ['check', 'cron'],
	methods: {
    // 單選按鈕值變化時
		radioChange() {
			if (this.radioValue !== 2 && this.cron.day !== '?') {
				this.$emit('update', 'day', '?', 'week')
			}
			switch (this.radioValue) {
				case 1:
					this.$emit('update', 'week', '*')
					break
				case 2:
					this.$emit('update', 'week', '?')
					break
				case 3:
					this.$emit('update', 'week', this.cycleTotal)
					break
				case 4:
					this.$emit('update', 'week', this.averageTotal)
					break
				case 5:
					this.$emit('update', 'week', this.weekdayCheck + 'L')
					break
				case 6:
					this.$emit('update', 'week', this.checkboxString)
					break
			}
		},

    // 週期兩個值變化時
		cycleChange() {
			if (this.radioValue == '3') {
				this.$emit('update', 'week', this.cycleTotal)
			}
		},
    // 平均兩個值變化時
		averageChange() {
			if (this.radioValue == '4') {
				this.$emit('update', 'week', this.averageTotal)
			}
		},
    // 最近工作日值變化時
		weekdayChange() {
			if (this.radioValue == '5') {
				this.$emit('update', 'week', this.weekday + 'L')
			}
		},
    // checkbox值變化時
		checkboxChange() {
			if (this.radioValue == '6') {
				this.$emit('update', 'week', this.checkboxString)
			}
		},
	},
	watch: {
		'radioValue': 'radioChange',
		'cycleTotal': 'cycleChange',
		'averageTotal': 'averageChange',
		'weekdayCheck': 'weekdayChange',
		'checkboxString': 'checkboxChange',
	},
	computed: {
    // 計算兩個週期值
		cycleTotal: function () {
			this.cycle01 = this.checkNum(this.cycle01, 1, 7)
			this.cycle02 = this.checkNum(this.cycle02, 1, 7)
			return this.cycle01 + '-' + this.cycle02
		},
    // 計算平均用到的值
		averageTotal: function () {
			this.average01 = this.checkNum(this.average01, 1, 4)
			this.average02 = this.checkNum(this.average02, 1, 7)
			return this.average02 + '#' + this.average01
		},
		// 最近的工作日（格式）
		weekdayCheck: function () {
			this.weekday = this.checkNum(this.weekday, 1, 7)
			return this.weekday
		},
    // 計算勾選的checkbox值合集
		checkboxString: function () {
			let str = this.checkboxList.join()
			return str == '' ? '*' : str
		}
	}
}
</script>
