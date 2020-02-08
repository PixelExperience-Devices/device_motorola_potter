/*
   Copyright (c) 2014, The Linux Foundation. All rights reserved.

   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions are
   met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following
      disclaimer in the documentation and/or other materials provided
      with the distribution.
    * Neither the name of The Linux Foundation nor the names of its
      contributors may be used to endorse or promote products derived
      from this software without specific prior written permission.

   THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
   WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT
   ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
   BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
   CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
   SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
   BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
   WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
   OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
   IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

#include <stdlib.h>
#define _REALLY_INCLUDE_SYS__SYSTEM_PROPERTIES_H_
#include <sys/_system_properties.h>
#include <sys/sysinfo.h>
#include <android-base/properties.h>
#include "property_service.h"
#include "vendor_init.h"

void property_override(char const prop[], char const value[], bool add = true)
{
    auto pi = (prop_info *) __system_property_find(prop);

    if (pi != nullptr) {
        __system_property_update(pi, value, strlen(value));
    } else if (add) {
        __system_property_add(prop, strlen(prop), value, strlen(value));
    }
}

void property_override_triple(char const product_prop[], char const system_prop[], char const vendor_prop[], char const value[])
{
    property_override(product_prop, value);
    property_override(system_prop, value);
    property_override(vendor_prop, value);
}

/* Get Ram size for different variants */
void check_device()
{
    struct sysinfo sys;
    sysinfo(&sys);
    if (sys.totalram > 3072ull * 1024 * 1024) {
        property_override("ro.boot.ram", "4GB");
    } else if (sys.totalram > 2048ull * 1024 * 1024) {
        property_override("ro.boot.ram", "3GB");
    } else {
        property_override("ro.boot.ram", "2GB");
    }
}

void num_sims() {
    std::string dualsim;

    dualsim = android::base::GetProperty("ro.boot.dualsim", "");
    property_override("ro.hw.dualsim", dualsim.c_str());

    if (dualsim == "true") {
        property_override("persist.radio.multisim.config", "dsds");
    } else {
        property_override("persist.radio.multisim.config", "");
    }
}

void vendor_load_properties()
{
    // fingerprint
    property_override("ro.build.description", "potter-7.0/NPNS25.137-33-11/11:user/release-keys");
    property_override_triple("ro.build.fingerprint", "ro.system.build.fingerprint", "ro.vendor.build.fingerprint", "google/coral/coral:10/QQ1B.200105.004/6031802:user/release-keys");

    // sku
    std::string sku = "Moto G5 Plus (";
    sku.append(android::base::GetProperty("ro.boot.hardware.sku", ""));
    sku.append(")");
    property_override("ro.product.model", sku.c_str());

    // rmt_storage
    std::string device = android::base::GetProperty("ro.boot.device", "");
    std::string radio = android::base::GetProperty("ro.boot.radio", "");
    property_override("ro.vendor.hw.device", device.c_str());
    property_override("ro.vendor.hw.radio", radio.c_str());
    property_override("ro.hw.fps", "true");
    property_override("ro.hw.imager", "12MP");

    num_sims();

    if (sku == "XT1687") {
        property_override("ro.hw.ecompass", "true");
        property_override("ro.hw.nfc", "false");
    }
    else {
        property_override("ro.hw.ecompass", "false");
        property_override("ro.hw.nfc", "true");
    }

    if (sku == "XT1683") {
        property_override("ro.hw.dtv", "true");
    }

    check_device();
}
