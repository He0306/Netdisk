<template>
    <div ref="player" id="player"></div>
</template>

<script setup>
import {getCurrentInstance, onMounted, ref} from "vue";
import DPlayer from "dplayer"
import Hls from "hls.js";

const {proxy} = getCurrentInstance()
const props = defineProps({
    url: {
        type: String
    }
})
const videInfo = ref({
    video: null
})
const player = ref()
const initPlayer = () => {
    const dp = new DPlayer({
        element: player.value,
        theme: "#b7daff",
        screenshot: true,
        video: {
            url: `/api/${props.url}`,
            type: "customHls",
            customType: {
                customHls: function (video, player) {
                    const hls = new Hls()
                    hls.loadSource(video.src)
                    hls.attachMedia(video)
                }
            }
        }
    })
}
onMounted(() => {
    initPlayer()
})
</script>

<style lang="scss" scoped>
#player {
    width: 100%;
    :deep(.dplayer-video-wrap) {
        text-align: center;
        .dplayer-video {
            margin: 0 auto;
            max-height: calc(100vh - 41px);
        }
    }
}
</style>