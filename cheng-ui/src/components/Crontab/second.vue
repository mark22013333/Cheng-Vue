<template>
	<el-form>
		<el-form-item>
			<el-radio v-model='radioValue' :label="1">
        秒，允許的共用符號[, - * /]
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="2">
        週期從
				<el-input-number v-model='cycle01' :min="0" :max="58" /> -
				<el-input-number v-model='cycle02' :min="cycle01 ? cycle01 + 1 : 1" :max="59" /> 秒
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="3">
        從
        <el-input-number v-model='average01' :max="58" :min="0"/>
        秒開始，每
        <el-input-number v-model='average02' :max="59 - average01 || 0" :min="1"/>
        秒執行一次
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="4">
				指定
        <el-select v-model="checkboxList" clearable multiple placeholder="請選擇" style="width:100%; min-width: 200px;">
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
			checkboxList: []
		}
	},
	name: 'crontab-second',
	props: ['check', 'radioParent'],
	methods: {
    // 單選按鈕值變化時
		radioChange() {
			switch (this.radioValue) {
				case 1:
					this.$emit('update', 'second', '*', 'second')
					break
				case 2:
					this.$emit('update', 'second', this.cycleTotal)
					break
				case 3:
					this.$emit('update', 'second', this.averageTotal)
					break
				case 4:
					this.$emit('update', 'second', this.checkboxString)
					break
			}
		},
    // 週期兩個值變化時
		cycleChange() {
			if (this.radioValue == '2') {
				this.$emit('update', 'second', this.cycleTotal)
			}
		},
    // 平均兩個值變化時
		averageChange() {
			if (this.radioValue == '3') {
				this.$emit('update', 'second', this.averageTotal)
			}
		},
    // checkbox值變化時
		checkboxChange() {
			if (this.radioValue == '4') {
				this.$emit('update', 'second', this.checkboxString)
			}
		}
	},
	watch: {
		'radioValue': 'radioChange',
		'cycleTotal': 'cycleChange',
		'averageTotal': 'averageChange',
		'checkboxString': 'checkboxChange',
		radioParent() {
			this.radioValue = this.radioParent
		}
	},
	computed: {
    // 計算兩個週期值
		cycleTotal: function () {
			const cycle01 = this.check(this.cycle01, 0, 58)
			const cycle02 = this.check(this.cycle02, cycle01 ? cycle01 + 1 : 1, 59)
			return cycle01 + '-' + cycle02
		},
    // 計算平均用到的值
		averageTotal: function () {
			const average01 = this.check(this.average01, 0, 58)
			const average02 = this.check(this.average02, 1, 59 - average01 || 0)
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
