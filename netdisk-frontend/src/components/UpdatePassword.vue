<template>
  <div>
    <!--  弹窗  -->
    <el-dialog v-model="dialogFormVisible" title="修改密码" width="450" center>
      <el-form label-position="right" :model="form" ref="formRef" :rules="formRules" label-width="100px">
        <el-form-item label="新密码" prop="password">
          <el-input type="password" show-password size="large" placeholder="请输入新密码" v-model.trim="form.password">
            <template #prefix>
              <span class="iconfont icon-password"></span>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="rePassword">
          <el-input type="password" show-password size="large" placeholder="请再次输入密码" v-model.trim="form.rePassword">
            <template #prefix>
              <span class="iconfont icon-password"></span>
            </template>
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
          <span class="dialog-footer">
              <el-button type="danger" @click="submitForm">确定</el-button>
          </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {getCurrentInstance, nextTick, ref} from "vue";
import {useRoute, useRouter} from "vue-router";

const route = useRoute()
const router = useRouter()

const {proxy} = getCurrentInstance()

const api = {
  updatePassword: "/updatePassword"
}


const dialogFormVisible = ref(false)
const form = ref({})
const formRef = ref()
const checkRefPassword = (rule, value, callback) => {
  if (value !== form.value.password) {
    callback(new Error(rule.message))
  } else {
    callback()
  }
}
const formRules = {
  password: [{required: true, message: "请输入密码", trigger: 'blur'},
    {pattern: /^(?=.*\d)(?=.*[a-zA-Z])(?=.*[^a-zA-Z0-9]).{8,18}$/, message: "只能是数字，字母，特殊字符 8-18位"}],
  rePassword: [{required: true, message: "请输入密码", trigger: 'blur'},
    {validator: checkRefPassword, message: "两次输入密码不一致"}]
}
const show = () => {
  dialogFormVisible.value = true
  nextTick(() => {
    formRef.value.resetFields()
    form.value = {}
  })
}
defineExpose({show})

const submitForm = async () => {
  formRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    let result = await proxy.Request({
      url: api.updatePassword,
      params: {
        password: form.value.password
      }
    })
    if (!result) {
      return
    }
    dialogFormVisible.value = false
    proxy.Message.success("密码修改成功！")
    proxy.VueCookies.remove("userInfo")
    router.push("/login")
  })
}
</script>

<style lang="scss" scoped>

</style>
