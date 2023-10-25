<template>
    <div class="code">
        <div class="top-op">
            <div class="encode-select">
                <el-select
                        clearable
                        placeholder="选择编码"
                        v-model="encode"
                        @change="changeEncode"
                >
                    <el-option value="UTF-8" label="UTF-8编码"></el-option>
                    <el-option value="GBK" label="gbk编码"></el-option>
                </el-select>
                <div class="tips">乱码了？切换编码</div>
            </div>
            <div class="copy-btn">
                <el-button type="primary" @click="copy">复制</el-button>
            </div>
        </div>
        <highlightjs autodetect :code="txtContent"></highlightjs>
    </div>
</template>

<script setup>
import useClipboard from "vue-clipboard3"
import {getCurrentInstance, onMounted, ref} from "vue";

const {toClipboard} = useClipboard()

const {proxy} = getCurrentInstance()

const props = defineProps({
    url: {
        type: String
    }
})
const codeRef = ref()
const txtContent = ref("")
const blobResult = ref()
const encode = ref("UTF-8")
const readTxt = async () => {
    let result = await proxy.Request({
        url: props.url,
        responseType: "blob"
    })
    if (!result) {
        return;
    }
    blobResult.value = result
    showTxt()
}

const changeEncode = (e) => {
    encode.value = e
    showTxt()
}
const showTxt = () => {
    const reader = new FileReader()
    reader.onload = () => {
        txtContent.value = reader.result
    }
    reader.readAsText(blobResult.value, encode.value)
}
onMounted(() => {
    readTxt()
})
const copy = async () => {
    await toClipboard(txtContent.value)
    proxy.Message.success("复制成功")
}
</script>

<style scoped lang="scss">
.code {
  width: 100%;

  .top-op {
    display: flex;
    align-items: center;
    justify-content: space-around;
  }

  .encode-select {
    flex: 1;
    display: flex;
    align-items: center;
    margin: 5px 10px;

    .tips {
      margin-left: 10px;
      color: #828282;
    }
  }

  .copy-btn {
    margin-right: 10px;
  }
}
</style>