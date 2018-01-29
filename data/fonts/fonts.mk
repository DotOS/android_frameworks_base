# Copyright (C) 2008 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Warning: this is actually a product definition, to be inherited from

ifneq ($(MULTI_LANG_ENGINE),REVERIE)
  PRODUCT_COPY_FILES := \
    frameworks/base/data/fonts/fonts.xml:$(TARGET_COPY_OUT_SYSTEM)/etc/fonts.xml \
    frameworks/base/data/fonts/big-noodle.ttf:$(TARGET_COPY_OUT_SYSTEM)/fonts/big-noodle.ttf \
    frameworks/base/data/fonts/big-noodle.ttf:$(TARGET_COPY_OUT_SYSTEM)/fonts/big-noodleItalic.ttf

endif

PRODUCT_PACKAGES := \
    DroidSansMono.ttf \
    AndroidClock.ttf \
    fonts.xml
