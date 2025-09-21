<template>
	<el-form size="small">
		<el-form-item>
			<el-radio v-model='radioValue' :label="1">
        分鐘，允許的通用符號[, - * /]
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="2">
        週期從
				<el-input-number v-model='cycle01' :min="0" :max="58" /> -
        <el-input-number v-model='cycle02' :max="59" :min="cycle01 ? cycle01 + 1 : 1"/>
        分鐘
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="3">
        從
        <el-input-number v-model='average01' :max="58" :min="0"/>
        分鐘開始，每
        <el-input-number v-model='average02' :max="59 - average01 || 0" :min="1"/>
        分鐘執行一次
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="4">
				指定
        <el-select v-model="checkboxList" clearable multiple placeholder="可多選" style="width:100%">
					<el-option v-for="item in 60" :key="item" :value="item-1">{{item-1}}</el-option>
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
			cycle01: 1,
			cycle02: 2,
			average01: 0,
			average02: 1,
			checkboxList: [],
			checkNum: this.$options.propsData.check
		}
	},
	name: 'crontab-min',
	props: ['check', 'cron'],
	methods: {
    // 單選按鈕值變化時
		radioChange() {
			switch (this.radioValue) {
				case 1:
					this.$emit('update', 'min', '*', 'min')
					break
				case 2:
					this.$emit('update', 'min', this.cycleTotal, 'min')
					break
				case 3:
					this.$emit('update', 'min', this.averageTotal, 'min')
					break
				case 4:
					this.$emit('update', 'min', this.checkboxString, 'min')
					break
			}
		},
    // 週期兩個值變化時
		cycleChange() {
			if (this.radioValue == '2') {
				this.$emit('update', 'min', this.cycleTotal, 'min')
			}
		},
    // 平均兩個值變化時
		averageChange() {
			if (this.radioValue == '3') {
				this.$emit('update', 'min', this.averageTotal, 'min')
			}
		},
    // checkbox值變化時
		checkboxChange() {
			if (this.radioValue == '4') {
				this.$emit('update', 'min', this.checkboxString, 'min')
			}
		},

	},
	watch: {
		'radioValue': 'radioChange',
		'cycleTotal': 'cycleChange',
		'averageTotal': 'averageChange',
		'checkboxString': 'checkboxChange',
	},
	computed: {
    // 計算兩個週期值
		cycleTotal: function () {
			const cycle01 = this.checkNum(this.cycle01, 0, 58)
			const cycle02 = this.checkNum(this.cycle02, cycle01 ? cycle01 + 1 : 1, 59)
			return cycle01 + '-' + cycle02
		},
    // 計算平均用到的值
		averageTotal: function () {
			const average01 = this.checkNum(this.average01, 0, 58)
			const average02 = this.checkNum(this.average02, 1, 59 - average01 || 0)
			return average01 + '/' + average02
		},
    // 計算勾選的checkbox值合集
		checkboxString: function () {
			let str = this.checkboxList.join()
			return str == '' ? '*' : str
		}
	}
}
</script>
