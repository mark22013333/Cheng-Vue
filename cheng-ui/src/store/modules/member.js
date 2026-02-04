import { defineStore } from 'pinia'
import { memberLogin, memberRegister, memberLogout, getMemberProfile } from '@/api/shop/auth'
import { getMemberToken, setMemberToken, removeMemberToken } from '@/utils/memberAuth'
import { isHttp, isEmpty } from '@/utils/validate'
import defAva from '@/assets/images/profile.jpg'

const useMemberStore = defineStore('member', {
  state: () => ({
    token: getMemberToken(),
    id: '',
    nickname: '',
    avatar: '',
    mobile: '',
    email: ''
  }),
  actions: {
    // 會員登入
    login(loginBody) {
      return new Promise((resolve, reject) => {
        memberLogin(loginBody).then(res => {
          setMemberToken(res.token)
          this.token = res.token
          this.setMember(res.member)
          resolve(res)
        }).catch(error => {
          reject(error)
        })
      })
    },
    // 會員註冊
    register(registerBody) {
      return new Promise((resolve, reject) => {
        memberRegister(registerBody).then(res => {
          setMemberToken(res.token)
          this.token = res.token
          this.setMember(res.member)
          resolve(res)
        }).catch(error => {
          reject(error)
        })
      })
    },
    // 取得會員資料
    getProfile() {
      return new Promise((resolve, reject) => {
        getMemberProfile().then(res => {
          this.setMember(res.data || res.member || res)
          resolve(res)
        }).catch(error => {
          reject(error)
        })
      })
    },
    // 會員登出
    logOut() {
      return new Promise((resolve, reject) => {
        memberLogout().then(() => {
          this.clearMember()
          resolve()
        }).catch(error => {
          reject(error)
        })
      })
    },
    setMember(member) {
      if (!member) return
      let avatar = member.avatar || ''
      if (!isHttp(avatar)) {
        avatar = (isEmpty(avatar)) ? defAva : import.meta.env.VITE_APP_BASE_API + avatar
      }
      this.id = member.memberId || ''
      this.nickname = member.nickname || ''
      this.avatar = avatar
      this.mobile = member.mobile || ''
      this.email = member.email || ''
    },
    clearMember() {
      this.token = ''
      this.id = ''
      this.nickname = ''
      this.avatar = ''
      this.mobile = ''
      this.email = ''
      removeMemberToken()
    }
  }
})

export default useMemberStore
