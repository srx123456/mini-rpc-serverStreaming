package io.github.lanicc.mrpc.test.api;

/**
 * @author suoruixiang
 * @date 2024-05-13
 * 存放的数据为 IP:port
 */
public class LocalRemoteMapper {

    private String local;

    private String remote;

    public LocalRemoteMapper() {
    }

    public LocalRemoteMapper(String local, String remote){
        this.local = local;
        this.remote = remote;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getRemote() {
        return remote;
    }

    public void setRemote(String remote) {
        this.remote = remote;
    }

    @Override
    public String toString() {
        return local+" : "+remote;
    }
}
