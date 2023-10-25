<template>
    <div>
        <el-dialog v-model="dialogFormVisible" title="分享" width="550" center>
            <el-form label-position="right" :model="formData" ref="formRef" :rules="formRules" label-width="100px">
                <el-form-item label="文件名：" prop="fileName">
                    {{ formData.fileName }}
                </el-form-item>
                <template v-if="showType === 0">
                    <el-form-item label="有效期：" prop="validType">
                        <el-radio-group v-model="formData.validType">
                            <el-radio :label="0">1天</el-radio>
                            <el-radio :label="1">7天</el-radio>
                            <el-radio :label="2">30天</el-radio>
                            <el-radio :label="3">永久有效</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item label="提取码：" prop="codeType">
                        <el-radio-group v-model="formData.codeType">
                            <el-radio :label="0">自定义</el-radio>
                            <el-radio :label="1">系统生成</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item prop="code" v-if="formData.codeType===0">
                        <el-input
                                clearable
                                placeholder="请输入5位提取码"
                                v-model="formData.code"
                                maxlength="5"
                                :style="{ width: '130px' }">
                        </el-input>
                    </el-form-item>
                </template>
                <template v-else>
                    <el-form-item label="分享链接">
                        {{ shareUrl }}{{ resultInfo.shareId }}
                    </el-form-item>
                    <el-form-item label="提取码">
                        {{ resultInfo.code }}
                    </el-form-item>
                    <el-form-item>
                        <el-button type="primary" @click="copy">复制链接</el-button>
                    </el-form-item>
                </template>
            </el-form>
            <template #footer>
          <span class="dialog-footer">
              <el-button type="primary" @click="submitForm" v-if="resultInfo !== null">确定</el-button>
              <el-button @click="cancle" v-if="resultInfo === null">关闭</el-button>
              <el-button @click="cancle" v-if="resultInfo !== null">取消</el-button>
          </span>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import useClipboard from "vue-clipboard3"
import {getCurrentInstance, nextTick, ref} from "vue";

const { toClipboard } = useClipboard()

const {proxy} = getCurrentInstance()
const api = {
    shareFile: "/share/shareFile"
}
const dialogFormVisible = ref(false)
const formData = ref({
    validType: 1
})
const shareUrl = ref(document.location.origin + "/share/")
const showType = ref(0)
const formRef = ref()

const show = (data) => {
    showType.value = 0
    dialogFormVisible.value = true
    nextTick(() => {
        formRef.value.resetFields()
        formData.value = Object.assign({}, data)
    })
}
defineExpose({show})
const resultInfo = ref({})
// 提交表单
const submitForm = async () => {
    if (Object.keys(resultInfo.value).length > 0) {
        dialogFormVisible.value = false
        return;
    }
    formRef.value.validate(async (valid) => {
        if (!valid) {
            return;
        }
        let params = {}
        Object.assign(params, formData.value)
        let result = await proxy.Request({
            url: api.shareFile,
            params: params
        })
        if (!result) {
            return
        }
        showType.value = 1
        resultInfo.value = result.data
    })
}
const copy = async () => {
    await toClipboard(`链接：${shareUrl.value}${resultInfo.value.shareId} 提取码，${resultInfo.value.code}`)
    proxy.Message.success("复制成功")
}
// 取消
const cancle = () => {
    dialogFormVisible.value = false
}
const formRules = {
    validType: [{required: true, message: "请选择有效期"}],
    codeType: [{required: true, message: "请选择提取码类型"}],
    code: [
        {required: true, message: "请输入提取码"},
        {min: 5, message: "提取码最少5位"}
    ]
}
</script>

<style scoped lang="scss">

</style>