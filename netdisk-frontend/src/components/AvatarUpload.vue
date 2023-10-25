<template>
  <div class="avatar-upload">
    <div class="avatar-show">
      <template v-if="localFile">
        <img :src="localFile"/>
      </template>
      <template v-else>
        <img :src="`${modeValue.qqAvatar}`" v-if="modeValue && modeValue.qqAvatar"/>
        <img :src="`/api/getAvatar/${modeValue.userId}`" v-else/>
      </template>
    </div>
    <div class="select-btn">
      <el-upload
          name="file"
          :show-file-list="false"
          accept=".png,.PNG,.jpg,.JPG,.jpeg,.JPEG,.gif,.GIF,.bmp,.BMP"
          :multiple="false"
          :http-request="uploadImage"
      >
        <el-button type="primary">选择</el-button>
      </el-upload>
    </div>

  </div>
</template>

<script setup>
import {getCurrentInstance, ref} from "vue";

const {proxy} = getCurrentInstance()
const userInfo = ref(proxy.VueCookies.get("userInfo"))

const timestamp = ref("")

const props = defineProps({
  modeValue: {
    type: Object,
    default: null
  }
})

const localFile = ref("")
const emit = defineEmits()
const uploadImage = async (file) => {
  file = file.file
  let img = new FileReader()
  img.readAsDataURL(file)
  img.onload = ({target}) => {
    console.log("target===>", target)
    localFile.value = target.result
  }
  emit("update:modeValue", file)
}

</script>

<style lang="scss" scoped>
.avatar-upload {
  display: flex;
  justify-content: center;
  align-items: end;

  .avatar-show {
    background: rgb(245, 245, 245);
    width: 150px;
    height: 150px;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    position: relative;

    img {
      width: 100%;
      object-fit: cover;
    }

    .iconfont {
      font-size: 50px;
      color: #ddd;
    }

    .op {
      position: absolute;
      color: #0e8aef;
      top: 80px;
    }
  }

  .select-btn {
    margin-left: 10px;
    vertical-align: bottom;
  }
}
</style>
