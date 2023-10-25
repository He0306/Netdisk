<template>
  <div>
    <!--  弹窗  -->
    <el-dialog v-model="dialogFormVisible" title="修改头像" width="450" center>
      <el-form label-width="100px" label-position="right" :model="form" ref="formRef" :rules="formRules">
        <el-form-item label="昵称">
          {{ form.nickName }}
        </el-form-item>
        <el-form-item label="头像">
          <AvatarUpload v-model:modeValue="form.avatar"></AvatarUpload>
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
import AvatarUpload from "../components/AvatarUpload.vue";
import {getCurrentInstance, ref} from "vue";

const {proxy} = getCurrentInstance()

const api = {
  updateUserAvatar: "/updateUserAvatar"
}

const dialogFormVisible = ref(false)
const formRef = ref()
const form = ref({})
const formRules = {}
const show = (data) => {
  form.value = Object.assign({}, data)
  form.value.avatar = {userId: data.userId, qqAvatar: data.avatar}
  dialogFormVisible.value = true
}
defineExpose({show})
// 提交
const emit = defineEmits()
const submitForm = async () => {
  console.log("avatar===>", form.value.avatar)
  if (!(form.value.avatar instanceof File)) {
    dialogFormVisible.value = false
    return;
  }
  let result = await proxy.Request({
    url: api.updateUserAvatar,
    params: {
      avatar: form.value.avatar
    }
  })

  if (!result) {
    return
  }
  dialogFormVisible.value = false
  const cookieUserInfo = proxy.VueCookies.get("userInfo")
  delete cookieUserInfo.avatar
  proxy.VueCookies.set("userInfo", cookieUserInfo, 0)
  emit("updateAvatar")
}
</script>

<style lang="scss" scoped>

</style>
