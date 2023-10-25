<template>
  <el-config-provider :locale="locale" :message="config">
    <router-view/>
  </el-config-provider>
</template>
<script setup>
import zhCn from 'element-plus/lib/locale/lang/zh-cn'
import {reactive} from "vue";

const locale = zhCn
const config = reactive({
  max: 1
})
</script>
<style lang="scss" scoped>

</style>
