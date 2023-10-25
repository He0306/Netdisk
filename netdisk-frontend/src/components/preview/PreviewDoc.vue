<template>
    <div ref="docRef" class="doc-content">
    </div>
</template>

<script setup>
import * as docx from "docx-preview"
import {getCurrentInstance, onMounted, ref} from "vue";

const {proxy} = getCurrentInstance()

const props = defineProps({
    url: {
        type: String
    }
})
const docRef = ref()
const initDoc = async () => {
    let result = await proxy.Request({
        url: props.url,
        responseType: "blob"
    })
    if (!result) {
        return;
    }
    console.log(result)
    docx.renderAsync(result, docRef.value);
}
onMounted(() => {
    initDoc()
})
</script>

<style scoped lang="scss">
.doc-content {
  margin: 0 auto;

  :deep(.docx-wrapper) {
    background: #fff;
    padding: 10px 0px;
  }

  :deep(.docx-wrapper > section.docx) {
    margin-bottom: 0px;
  }
}
</style>