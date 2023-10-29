<template>
<div>
  <div class="sys-setting-panel">
      <el-form :model="formData" :rules="rules" ref="formDataRef" @submit.prevent :label-width="180">
          <el-form-item label="注册邮箱标题" prop="registerEmailTitle">
              <el-input
                      clearable
                      placeholder="请输入注册邮箱标题"
                      v-model.trim="formData.registerEmailTitle">
              </el-input>
          </el-form-item>
          <el-form-item label="注册邮箱内容" prop="registerEmailContent">
              <el-input
                      clearable
                      placeholder="请输入注册邮箱内容"
                      v-model.trim="formData.registerEmailContent">
              </el-input>
          </el-form-item>
          <el-form-item label="初始化空间大小" prop="userInitUserSpace">
              <el-input
                      clearable
                      placeholder="请输入初始化空间大小"
                      v-model.trim="formData.userInitUserSpace">
                  <template #suffix>MB</template>
              </el-input>
          </el-form-item>
          <el-form-item>
              <el-button type="primary" @click="saveSettings">保存</el-button>
          </el-form-item>
      </el-form>
  </div>
</div>
</template>

<script setup>
import {getCurrentInstance, ref} from "vue";
const {proxy} = getCurrentInstance()

const api = {
    getSysSettings: "/admin/getSysSettings",
    saveSysSettings: "/admin/saveSysSettings"
}
const formData = ref({})
const rules = {
    registerEmailTitle: [{required: true, message: "请输入注册邮箱标题", trigger: 'blur'}],
    registerEmailContent: [{required: true, message: "请输入注册邮箱内容", trigger: 'blur'}],
    userInitUserSpace: [{required: true, message: "请输入初始化空间大小", trigger: 'blur'},
        { type: 'number', message: '请输入数字' }]
}
const formDataRef = ref()
// 加载系统设置信息
const getSysSettings = async () =>{
    let result = await proxy.Request({
        url: api.getSysSettings
    })
    if (!result){
        return
    }
    formData.value = result.data
}
getSysSettings()

const saveSettings = async ()=>{
    formDataRef.value.validate(async (valid) =>{
        if (!valid){
            return;
        }
        let params = {}
        Object.assign(params,formData.value)
        let result = await proxy.Request({
            url: api.saveSysSettings,
            params
        })
        if (!result){
            return
        }
        proxy.Message.success("保存成功")
    })
}
</script>

<style lang="scss" scoped>
.sys-setting-panel {
  width: 600px;
  margin-top: 150px;
  margin-left: 180px;
}
</style>
