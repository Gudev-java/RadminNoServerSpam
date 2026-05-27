package com.radmin.spam.filter.mixin;

import net.minecraft.client.server.LanServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

@Mixin(targets = "net.minecraft.client.server.LanServerDetection$LanServerList")
public abstract class LanServerListMixin {
    @Shadow @Final private List<LanServer> servers;

    @Inject(method = "addServer", at = @At("HEAD"), cancellable = true)
    private void addServer(String motd, InetAddress address, CallbackInfo ci) {
        String s1 = parse(motd);
        String ip = address.getHostAddress();

        for (LanServer s : this.servers) {
            String addr = s.getAddress();
            String s_ip = addr.contains(":") ? addr.split(":")[0] : addr;

            if (s_ip.equals(ip)) {
                if (sim(s1, s.getMotd())) {
                    ci.cancel();
                    return;
                }
                
                if (count(ip) >= 2 && isSpam(s1)) {
                    ci.cancel();
                    return;
                }
            }
            
            if (isSpam(s1) && s1.equalsIgnoreCase(s.getMotd())) {
                ci.cancel();
                return;
            }
        }
    }

    @Inject(method = "takeDirtyServers", at = @At("RETURN"), cancellable = true)
    private void takeDirty(CallbackInfoReturnable<List<LanServer>> cir) {
        List<LanServer> l = cir.getReturnValue();
        if (l == null || l.size() <= 1) return;

        List<LanServer> list = new ArrayList<>(l);
        list.sort((a, b) -> {
            boolean sp1 = isSpam(a.getMotd());
            boolean sp2 = isSpam(b.getMotd());
            if (sp1 && !sp2) return 1;
            if (!sp1 && sp2) return -1;
            return 0;
        });

        cir.setReturnValue(List.copyOf(list));
    }

    private String parse(String s) {
        int i = s.indexOf("[MOTD]");
        if (i < 0) return s;
        int j = s.indexOf("[/MOTD]", i + 6);
        if (j < i) return s;
        return s.substring(i + 6, j);
    }

    private boolean sim(String a, String b) {
        if (a == null || b == null) return false;
        String c1 = a.replaceAll("[^a-zA-Zа-яА-Я]", "");
        String c2 = b.replaceAll("[^a-zA-Zа-яА-Я]", "");
        if (c1.isEmpty() || c2.isEmpty()) return a.equals(b);
        return c1.equalsIgnoreCase(c2);
    }

    private boolean isSpam(String s) {
        if (s == null) return false;
        String low = s.toLowerCase();
        if (low.contains("radmin") || low.contains("antiattack") || low.contains("спам")) return true;
        if (low.contains("t.me") || low.contains("http") || low.contains("@")) return true;
        if (low.contains("player sample") || low.contains("скрой ники")) return true;
        if (s.matches(".*#\\d{2,}$")) return true;
        return s.length() > 60;
    }

    private int count(String ip) {
        int c = 0;
        for (LanServer s : this.servers) {
            String addr = s.getAddress();
            String s_ip = addr.contains(":") ? addr.split(":")[0] : addr;
            if (s_ip.equals(ip)) c++;
        }
        return c;
    }
}
