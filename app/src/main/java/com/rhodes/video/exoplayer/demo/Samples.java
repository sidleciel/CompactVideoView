/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rhodes.video.exoplayer.demo;

import com.google.android.exoplayer.util.Util;

import java.util.Locale;

/**
 * Holds statically defined sample definitions.
 */
/* package */ class Samples {

    public static class Sample {

        public final String name;
        public final String contentId;
        public final String provider;
        public final String uri;
        public final int    type;

        public Sample(String name, String uri, int type) {
            this(name,//视频名称
                    name.toLowerCase(Locale.US).replaceAll("\\s", ""),//contentID
                    "",//provider
                    uri,//视频连接
                    type);//类型
        }

        public Sample(String name, String contentId, String provider, String uri, int type) {
            this.name = name;
            this.contentId = contentId;
            this.provider = provider;
            this.uri = uri;
            this.type = type;
        }

    }

    //杂项
    public static final Sample[] MISC = new Sample[]{
            new Sample("big_buck_bunny", "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4", Util.TYPE_OTHER),
            new Sample("Dizzy", "http://html5demos.com/assets/dizzy.mp4", Util.TYPE_OTHER),
            new Sample("touyiao", "http://v7.pstatp.com/d91564e1f2f289ea68337a2f1d406c1c/56e03036/origin/9651/4496054197", Util.TYPE_OTHER),
            new Sample("刺客信条", "http://7xph03.media1.z0.glb.clouddn.com/%E3%80%8A%E5%88%BA%E5%AE%A2%E4%BF%A1%E6%9D%A1%EF%BC%9A%E5%85%84%E5%BC%9F%E4%BC%9A%E3%80%8B%E9%A6%96%E6%AE%B5CG%E9%A2%84%E5%91%8A%E7%89%87_%E8%B6%85%E6%B8%85.mp4", Util.TYPE_OTHER),
            new Sample("《剑与魔法》评测_高清", "http://7xph03.media1.z0.glb.clouddn.com/%E3%80%90%E4%B8%80%E5%8F%AF%E7%9A%84%E7%8C%AB%E7%B2%AE%E8%AE%A1%E5%88%92%E3%80%91%E3%80%8A%E5%89%91%E4%B8%8E%E9%AD%94%E6%B3%95%E3%80%8B%E8%AF%84%E6%B5%8B_%E9%AB%98%E6%B8%85_1.mp4", Util.TYPE_OTHER),
            new Sample("2016CG混剪", "http://7xph03.media1.z0.glb.clouddn.com/2016CG%E6%B7%B7%E5%89%AA.mp4.mp4", Util.TYPE_OTHER),
            new Sample("minecraft-我的世界：史蒂夫的危机", "http://112.65.220.27/7xph03.media1.z0.glb.clouddn.com/%5B%E7%AC%AC%E4%B8%89%E9%9B%86%5D%E4%B9%90%E9%AB%98minecraft-%E6%88%91%E7%9A%84%E4%B8%96%E7%95%8C%EF%BC%9A%E5%8F%B2%E8%92%82%E5%A4%AB%E7%9A%84%E5%8D%B1%E6%9C%BA_%E6%A0%87%E6%B8%85.mp4?wsiphost=local", Util.TYPE_OTHER),
    };

    private Samples() {
    }

}
