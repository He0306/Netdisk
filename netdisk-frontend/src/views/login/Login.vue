<template>
  <div class="login-body">
    <div class="bg"></div>
    <div class="login-panel">
      <el-form class="login-register" :model="formData" :rules="rules" ref="formDataRef" @submit.prevent>
        <div class="login-title" v-if="opType === 1">云盘</div>
        <div class="login-title" v-if="opType === 0">注册</div>
        <div class="login-title" v-if="opType === 2">重置密码</div>
        <el-form-item prop="email">
          <el-input size="large" clearable placeholder="请输入邮箱" v-model.trim="formData.email" maxlength="150">
            <template #prefix>
              <span class="iconfont icon-account"></span>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password" v-if="opType === 1">
          <el-input type="password" show-password size="large" clearable placeholder="请输入密码"
                    v-model.trim="formData.password">
            <template #prefix>
              <span class="iconfont icon-password"></span>
            </template>
          </el-input>
        </el-form-item>

        <!--    注册    -->
        <div v-if="opType===0 || opType === 2">
          <el-form-item prop="emailCode">
            <div class="send-email-panel">
              <el-input size="large" clearable placeholder="请输入邮箱验证码"
                        v-model.trim="formData.emailCode">
                <template #prefix>
                  <span class="iconfont icon-checkcode"></span>
                </template>
              </el-input>
              <el-button class="send-mail-btn" type="primary" size="large" @click="getEmailCode()">获取验证码</el-button>
            </div>
            <el-popover placement="left" :width="500" trigger="click">
              <div>
                <p>1、在垃圾箱中查找邮箱验证码</p>
                <p>2、在邮箱中头像 -> 设置 -> 反垃圾 -> 白名单 -> 设置邮件地址白名单</p>
                <p>3、将邮箱【2740860037@qq.com】添加到白名单不知道怎么设置？</p>
              </div>
              <template #reference>
                <span class="a-link" :style="{'font-size':'13px'}">未收到邮箱验证码？</span>
              </template>
            </el-popover>
          </el-form-item>
          <el-form-item v-if="opType === 0" prop="nickName">
            <el-input size="large" placeholder="请输入昵称" v-model.trim="formData.nickName" maxlength="20">
              <template #prefix>
                <span class="iconfont icon-account"></span>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="registerPassword">
            <el-input type="password" show-password size="large" clearable placeholder="请输入密码"
                      v-model.trim="formData.registerPassword">
              <template #prefix>
                <span class="iconfont icon-password"></span>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="reRegisterPassword">
            <el-input type="password" show-password size="large" clearable placeholder="请再次输入密码"
                      v-model.trim="formData.reRegisterPassword">
              <template #prefix>
                <span class="iconfont icon-password"></span>
              </template>
            </el-input>
          </el-form-item>
        </div>

        <!--     验证码   -->
        <el-form-item prop="checkCode">
          <div class="check-code-panel">
            <el-input size="large" clearable placeholder="请输入图片验证码" v-model.trim="formData.checkCode"
                      @keyup.enter="doSubmit">
              <template #prefix>
                <span class="iconfont icon-checkcode"></span>
              </template>
            </el-input>
            <img :src="checkCodeUrl" class="check-code" @click="changeCheckCode(0)"/>
          </div>
        </el-form-item>

        <!--  登录      -->
        <el-form-item v-if="opType === 1">
          <div class="rememberme-panel">
            <el-checkbox v-model="formData.rememberMe">记住我</el-checkbox>
          </div>
          <div class="no-account">
            <a href="javascript:void(0)" class="a-link" @click="showPanel(2)">忘记密码？</a>
            <a href="javascript:void(0)" class="a-link" @click="showPanel(0)">没有账号</a>
          </div>
        </el-form-item>
        <!--    找回密码    -->
        <el-form-item v-if="opType === 2">
          <div class="no-account">
            <a href="javascript:void(0)" class="a-link" @click="showPanel(1)">去登录？</a>
          </div>
        </el-form-item>
        <!--   注册     -->
        <el-form-item v-if="opType === 0">
          <div class="no-account">
            <a href="javascript:void(0)" class="a-link" @click="showPanel(1)">已有账号？</a>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="no-btn" size="large" @click="doSubmit">
            <span v-if="opType === 1">登录</span>
            <span v-if="opType === 0">注册</span>
            <span v-if="opType === 2">重置密码</span>
          </el-button>
        </el-form-item>
        <div class="login-btn-qq" v-if="opType ===1">
          快捷登录<img src="@/assets/qq.jpeg" @click="qqLogin">
        </div>
      </el-form>
    </div>

    <!--  弹窗  -->
    <el-dialog v-model="dialogFormVisible" title="发送邮箱验证码" width="450" center>
      <el-form :model="form" ref="formRef" :rules="formRules">
        <el-form-item label="邮箱号">
          {{ formData.email }}
        </el-form-item>
        <el-form-item label="验证码" prop="checkCode">
          <div class="check-code-panel">
            <el-input size="large" clearable placeholder="请输入验证码" v-model.trim="form.checkCode">
              <template #prefix>
                <span class="iconfont icon-checkcode"></span>
              </template>
            </el-input>
            <img :src="checkCodeUrlSendEmail" class="check-code" @click="changeCheckCode(1)"/>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
          <span class="dialog-footer">
              <el-button type="danger" @click="sendEmailCode">发送邮箱验证码</el-button>
          </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
onMounted(() => {
  changeCheckCode(0)
  showPanel(1)
})
import {useRoute, useRouter} from "vue-router";
import {getCurrentInstance, nextTick, onMounted, ref} from "vue";

const router = useRouter()
const route = useRoute()
const {proxy} = getCurrentInstance()

// 发送邮箱验证码
const formRef = ref()
const form = ref({})
const formRules = {
  checkCode: [{required: true, message: "请输入邮箱验证码", trigger: 'blur'}],
}
const dialogFormVisible = ref(false)
const getEmailCode = () => {
  formDataRef.value.validateField("email", (valid) => {
    if (!valid) {
      return;
    }
    dialogFormVisible.value = true
    nextTick(() => {
      changeCheckCode(1)
      formRef.value.resetFields()
      form.value = {
        email: formData.value.email
      }
    })

  })
}

// 发送邮箱验证码
const sendEmailCode = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) {
      return;
    }
    const params = Object.assign({}, form.value)
    console.log(params)
    params.type = opType.value === 0 ? 0 : 1
    let result = await proxy.Request({
      url: api.sendEmailCode,
      params: params,
      errorCallback: () => {
        changeCheckCode(1)
      }
    })
    if (!result) {
      return
    }
    proxy.Message.success("验证码发送成功，请登录邮箱查看！")
    dialogFormVisible.value = false
  })
}

// 操作类型 0：注册  1：登录  2：重置密码
const opType = ref(1)
const showPanel = (type) => {
  opType.value = type
  restForm()
}
// 全局请求路径
const api = {
  checkCode: "api/checkCode",
  sendEmailCode: '/sendEmailCode',
  register: '/register',
  login: '/login',
  qqlogin: '/qqlogin',
  resetPwd: '/resetPwd'
}
const checkCodeUrl = ref(api.checkCode)
const checkCodeUrlSendEmail = ref(api.checkCode)
const changeCheckCode = (type) => {
  if (type === 0) {
    checkCodeUrl.value = api.checkCode + "?type=" + type + "&time=" + new Date().getTime()
  } else {
    checkCodeUrlSendEmail.value = api.checkCode + "?type=" + type + "&time=" + new Date().getTime()
  }
}

const checkRefPassword = (rule, value, callback) => {
  if (value !== formData.value.registerPassword) {
    callback(new Error(rule.message))
  } else {
    callback()
  }
}
const formData = ref({
  email: '2740860037@qq.com',
  password: 'Hc020310..'
})
const formDataRef = ref({})
const rules = {
  email: [{type: 'email', required: true, message: "请输入正确的邮箱", trigger: 'blur'}],
  password: [{required: true, message: "请输入密码", trigger: 'blur'}],
  emailCode: [{required: true, message: "请输入邮箱验证码", trigger: 'blur'}],
  nickName: [{required: true, message: "请输入昵称", trigger: 'blur'}],
  registerPassword: [{required: true, message: "请输入密码", trigger: 'blur'},
    {pattern: /^(?=.*\d)(?=.*[a-zA-Z])(?=.*[^a-zA-Z0-9]).{8,18}$/, message: "只能是数字，字母，特殊字符 8-18位"}],
  reRegisterPassword: [{required: true, message: "请输入密码", trigger: 'blur'},
    {validator: checkRefPassword, message: "两次输入密码不一致"}],
  checkCode: [{required: true, message: "请输入图片验证码", trigger: 'blur'},
      { min: 5, max: 5, message: '验证码长度为5位' }]
}

// 重置表单
const restForm = () => {
  changeCheckCode(0)
  formDataRef.value.resetFields()
  formData.value = {}
  // 登录
  if (opType.value === 1) {
    const cookieLoginInfo = proxy.VueCookies.get("loginInfo")
    if (cookieLoginInfo) {
      formData.value = cookieLoginInfo
    }
  }
}
// 注册、登录、重置密码 提交
const doSubmit = () => {
  formDataRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    let params = {}
    Object.assign(params, formData.value);
    // 注册  找回密码
    if (opType.value === 0 || opType.value === 2) {
      params.password = params.registerPassword
      delete params.registerPassword
      delete params.reRegisterPassword
    }
    // 登录
    if (opType.value === 1) {
      let cookieLoginInfo = proxy.VueCookies.get("loginInfo")
    }
    let url = null
    if (opType.value === 0) {
      url = api.register
    } else if (opType.value === 1) {
      url = api.login
    } else {
      url = api.resetPwd
    }

    let result = await proxy.Request({
      url: url,
      params: params,
      errorCallback: () => {
        changeCheckCode(0)
      }
    })
    if (!result) {
      return
    }
    // 注册返回
    if (opType.value === 0) {
      proxy.Message.success("注册成功，请登录！")
      showPanel(1)
    } else if (opType.value === 1) {
      if (params.rememberMe) {
        const loginInfo = {
          email: params.email,
          password: params.password,
          rememberMe: params.rememberMe
        }
        proxy.VueCookies.set("loginInfo", loginInfo, "7d")
      } else {
        proxy.VueCookies.remove("loginInfo")
      }
      proxy.Message.success("登录成功！")
      // 存储cookie
      proxy.VueCookies.set("userInfo", result.data, 0)
      // 重定向到原始页面
      const redirectUrl = route.query.redirectUrl || "/"
      router.push(redirectUrl)
    } else if (opType.value === 2) {
      // 重置密码
      proxy.Message.success("重置密码成功，请登录！")
      showPanel(1)
    }
  })
}
// QQ登录
const qqLogin = async () => {
  let result = await proxy.Request({
    url: api.qqlogin,
    params: {
      callbackUrl: route.query.redirectUrl || ''
    }
  })
  if (!result) {
    return;
  }
  proxy.VueCookies.remove("userInfo")
  document.location.href = result.data
}
</script>

<style lang="scss" scoped>
.login-body {
  height: calc(100vh);
  background-size: cover;
  background: url("../src/assets/login_bg.jpg");
  display: flex;

  .bg {
    flex: 1;
    background-size: cover;
    background-position: center;
    background-size: 650px;
    background-repeat: no-repeat;
    background-image: url("../src/assets/login_img.png");
  }

  .login-panel {
    width: 430px;
    margin-right: 15%;
    margin-top: calc((100vh - 500px) / 2);

    .login-register {
      padding: 25px;
      background: #ffffff;
      border-radius: 5px;

      .login-title {
        text-align: center;
        font-size: 18px;
        font-weight: bold;
        margin-bottom: 20px;
      }

      .send-email-panel {
        display: flex;
        width: 100%;
        justify-content: space-between;

        .send-mail-btn {
          margin-left: 5px;
        }
      }

      .rememberme-panel {
        width: 100%;
      }

      .no-account {
        width: 100%;
        display: flex;
        justify-content: space-between;
      }

      .no-btn {
        width: 100%;
      }
    }
  }

  .check-code-panel {
    width: 100%;
    display: flex;

    .check-code {
      margin-left: 5px;
      cursor: pointer;
    }
  }

  .login-btn-qq {
    margin-top: 20px;
    text-align: center;
    display: flex;
    align-items: center;
    justify-content: center;

    img {
      cursor: pointer;
      margin-left: 10px;
      width: 30px;
    }
  }
}
</style>
