<template>
    <div>
        <!-- 图片预览 -->
        <PreviewImage
                ref="imageViewRef"
                :imageList="[imageUrl]"
                v-if="fileInfo.fileCategory===3"
        >
        </PreviewImage>
        <!-- 弹窗大小 -->
        <Window
                :show="windowShow"
                @close="closeWindow"
                :width="fileInfo.fileCategory === 1 ? 1200 : 870"
                :title="fileInfo.fileName"
                :align="fileInfo.fileCategory === 1 ? 'center' : 'top'"
        >
            <PreviewVideo :url="url" v-if="fileInfo.fileCategory === 1"></PreviewVideo>
            <PreviewDoc :url="url" v-if="fileInfo.fileType === 5"></PreviewDoc>
            <PreviewExcel :url="url" v-if="fileInfo.fileType === 6"></PreviewExcel>
            <PreviewPdf :url="url" v-if="fileInfo.fileType === 4"></PreviewPdf>
            <PreviewTxt :url="url" v-if="fileInfo.fileType === 7 || fileInfo.fileType === 8"></PreviewTxt>
            <PreviewMusic
                    :url="url"
                    :fileName="fileInfo.fileName"
                    v-if="fileInfo.fileCategory === 2">
            </PreviewMusic>
            <PreviewDownload
                    :createDownloadUrl="createDownloadUrl"
                    :downloadUrl="downloadUrl"
                    :fileInfo="fileInfo"
                    v-if="fileInfo.fileCategory === 5 && fileInfo.fileType !== 8">
            </PreviewDownload>
        </Window>

    </div>
</template>

<script setup>
import PreviewImage from "@/components/preview/PreviewImage.vue";
import {computed, getCurrentInstance, nextTick, ref} from "vue";
import PreviewVideo from "@/components/preview/PreviewVideo.vue";
import PreviewDoc from "@/components/preview/PreviewDoc.vue";
import PreviewExcel from "@/components/preview/PreviewExcel.vue";
import PreviewPdf from "@/components/preview/PreviewPdf.vue";
import PreviewTxt from "@/components/preview/PreviewTxt.vue";
import PreviewMusic from "@/components/preview/PreviewMusic.vue";
import PreviewDownload from "@/components/preview/PreviewDownload.vue";

const {proxy} = getCurrentInstance()

const imageUrl = computed(() => {
    return proxy.globalInfo.imageUrl + fileInfo.value.fileCover.replaceAll("_.", ".")
})

const FILE_URL_MAP = {
    0: {
        fileUrl: "file/getFile",
        videoUrl: "file/ts/getVideoInfo",
        createDownLoadUrl: "file/createDownLoadUrl",
        downloadUrl: "api/file/download"
    },
    1: {
        fileUrl: "admin/getFile",
        videoUrl: "admin/ts/getVideoInfo",
        createDownLoadUrl: "admin/createDownLoadUrl",
        downloadUrl: "api/admin/download"
    },
    2: {
        fileUrl: "showShare/getFile",
        videoUrl: "showShare/ts/getVideoInfo",
        createDownLoadUrl: "showShare/createDownLoadUrl",
        downloadUrl: "api/showShare/download"
    }
}
const createDownloadUrl = ref(null)
const downloadUrl = ref(null)
const url = ref('')
const fileInfo = ref({})
const imageViewRef = ref()
const windowShow = ref(false)
const closeWindow = () => {
    windowShow.value = false
}
const showPreview = (data, showPart) => {
    fileInfo.value = data
    if (data.fileCategory === 3) {
        nextTick(() => {
            imageViewRef.value.show(0)
        })
    } else {
        windowShow.value = true
        let _url = FILE_URL_MAP[showPart].fileUrl
        let _createDownloadUrl = FILE_URL_MAP[showPart].createDownLoadUrl
        let _downloadUrl = FILE_URL_MAP[showPart].downloadUrl
        if (data.fileCategory === 1) {
            _url = FILE_URL_MAP[showPart].videoUrl
        }
        if (showPart === 0) {
            _url = _url + "/" + data.fileId
            _createDownloadUrl = _createDownloadUrl + "/" + data.fileId
        }
        if (showPart === 1) {
            _url = _url + "/" + data.userId + "/" + data.fileId
            _createDownloadUrl = _createDownloadUrl + "/" + data.userId + "/" + data.fileId
        }
        if (showPart === 2){
            _url = _url + "/" + data.shareId + "/" + data.fileId
            _createDownloadUrl = _createDownloadUrl + "/" + data.shareId + "/" + data.fileId
        }
        url.value = _url
        createDownloadUrl.value = _createDownloadUrl
        downloadUrl.value = _downloadUrl
    }
}
defineExpose({showPreview})
</script>

<style lang="scss" scoped>

</style>