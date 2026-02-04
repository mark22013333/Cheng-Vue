import Cookies from 'js-cookie'

const TokenKey = 'Member-Token'

export function getMemberToken() {
  return Cookies.get(TokenKey)
}

export function setMemberToken(token) {
  return Cookies.set(TokenKey, token)
}

export function removeMemberToken() {
  return Cookies.remove(TokenKey)
}
