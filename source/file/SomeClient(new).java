
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class SomeClient {
    private String uploadUrl;
    private String checkUrl;
    private String downloadUrl;

    public String getUploadUrl() {
        return uploadUrl;
    }

    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }

    public String getCheckUrl() {
        return checkUrl;
    }

    public void setCheckUrl(String checkUrl) {
        this.checkUrl = checkUrl;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public boolean uploadToICR(String username, String password, ICRUploadDTO udto) throws ICRClientException {
        HttpClient httpClient = new HttpClient();
        PostMethod post = null;
        try {
            checkStringParamEmpty(username, "username");
            checkStringParamEmpty(password, "password");
            checkStringParamEmpty(udto.getUrlPath(), "urlPath");
            checkStringParamEmpty(udto.getDocumentId(), "documentId");
            checkStringParamEmpty(udto.getTemplateId(), "templateId");
            checkStringParamEmpty(udto.getPages(), "pages");
            checkStringParamEmpty(udto.getReturnWay(), "returnWay");

            checkValueWithinList(udto.getPriority());

            if (udto.getReturnWay().equals("0")) {
                checkStringParamEmpty(udto.getReturnAddr(), "returnAddr");
            }
            EnCode ec = EnCode.getInstance();
            XStream xs = new XStream(new DomDriver());
            String str = xs.toXML(udto);
            post = new PostMethod(uploadUrl);
            post.addParameter("username", username);
            post.addParameter("password", ec.encryptionString(password));
            post.addParameter("str", str);
            int code = executeHttpClient(httpClient, post);
            return returnFinialResult(post, code);
        } catch (Exception e) {
            throw new ICRClientException(e.getMessage(), e);
        } finally {
            if (post != null) {
                post.releaseConnection();
            }
        }
    }

    public List checkForTask(String username, String password, List list)
            throws ICRClientException {
        HttpClient httpClient = new HttpClient();
        PostMethod post = null;
        try {
            checkStringParamEmpty(username, "username");
            checkStringParamEmpty(password, "password");
            checkListNoNull(list);
            Iterator ite = list.iterator();
            while (ite.hasNext()) {
                ICRUploadDTO udto = (ICRUploadDTO) ite.next();
                checkStringParamEmpty(udto.getUrlPath(), "urlPath");
                checkStringParamEmpty(udto.getDocumentId(), "documentId");
                checkValueWithinList(udto.getPriority());
            }

            XStream xs = new XStream(new DomDriver());
            String strlist = xs.toXML(list);
            EnCode ec = EnCode.getInstance();
            post = new PostMethod(checkUrl);
            post.addParameter("username", username);
            post.addParameter("password", ec.encryptionString(password));
            post.addParameter("strlist", strlist);
            int code = executeHttpClient(httpClient, post);

            return returnFinialResultList(post, xs, code);
        } catch (Exception e) {
            throw new ICRClientException(e.getMessage(), e);
        } finally {
            if (post != null) {
                post.releaseConnection();
            }
        }
    }

    public ICRFileMatter downloadFromICR(String username,
                                         String password, ICRUploadDTO udto) throws ICRClientException {
        HttpClient httpClient = new HttpClient();
        PostMethod post = null;
        try {
            if (udto == null)
                throw new ICRClientException("ICRUploadDTO can not be null");

            checkStringParamEmpty(username, "username");
            checkStringParamEmpty(password, "password");
            checkStringParamEmpty(udto.getUrlPath(), "urlPath");
            checkValueWithinList(udto.getPriority());
            checkStringParamEmpty(udto.getDocumentId(), "documentId");

            EnCode ec = EnCode.getInstance();
            XStream xs = new XStream(new DomDriver());
            String str = xs.toXML(udto);
            post = new PostMethod(downloadUrl);
            post.addParameter("username", username);
            post.addParameter("password", ec.encryptionString(password));
            post.addParameter("str", str);
            int code = executeHttpClient(httpClient, post);

            return returnFinialResultMatter(post, code);
        } catch (Exception e) {
            throw new ICRClientException(e.getMessage(), e);
        } finally {
            if (post != null) {
                post.releaseConnection();
            }
        }
    }

    private ICRFileMatter returnFinialResultMatter(PostMethod post, int code) throws ICRClientException, IOException {
        checkCodeIsOK(post, code);
        ICRFileMatter fm = new ICRFileMatter();
        fm.setFileContent(IOUtils.toByteArray(post.getResponseBodyAsStream()));
        fm.setFilename(post.getResponseHeader("filename").getValue());
        return fm;
    }

    private void checkStringParamEmpty(String value, String name) throws ICRClientException {
        if (StringUtil.isEmpty(value)) {
            throw new ICRClientException(name + " can not be null");
        }
    }

    private void checkValueWithinList(String value) throws ICRClientException {
        if (!Arrays.asList("0", "1", "2", "3").contains(value)) {
            throw new ICRClientException("priority must be 0/1/2/3");
        }
    }

    private int executeHttpClient(HttpClient httpClient, PostMethod post) throws ICRClientException, IOException {
        return httpClient.executeMethod(post);
    }

    private void checkListNoNull(List list) throws ICRClientException {
        if (list.isEmpty())
            throw new ICRClientException("list can not be null");
    }

    private boolean returnFinialResult(PostMethod post, int code) throws ICRClientException, IOException {
        checkCodeIsOK(post, code);
        return post.getResponseBodyAsString().equals("ok");
    }

    private List returnFinialResultList(PostMethod post, XStream xs, int code) throws ICRClientException, IOException {
        checkCodeIsOK(post, code);
        return (List) xs.fromXML(post.getResponseBodyAsString());
    }

    private void checkCodeIsOK(PostMethod post, int code) throws ICRClientException, IOException {
        if (code == 500) throw new ICRClientException(post.getResponseBodyAsString());
        if (code != 200) throw new ICRClientException(code + ":" + post.getStatusText());
    }

}
