<template>
  <div>登录中，请勿刷新......</div>
</template>

<script setup>
import {useRoute, useRouter} from "vue-router";
import {getCurrentInstance} from "vue";

const router = useRouter()
const route = useRoute()
const {proxy} = getCurrentInstance()
const api = {
  logincallback: "/qqlogin/callback"
}

const login = async () => {
  let result = await proxy.Request({
    url: api.logincallback,
    params: router.currentRoute.value.query,
    errorCallback: () => {
      router.push("/")
    }
  })
  if (!result) {
    return
  }
  let redirectUrl = result.data.errorCallback || "/"
  if (redirectUrl === "/login") {
    redirectUrl = "/"
  }
  // 存储cookie
  proxy.VueCookies.set("userInfo", result.data, 0)
  router.push(redirectUrl)
}
</script>

<style lang="scss" scoped>

</style>
