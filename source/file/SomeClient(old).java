
import java.io.IOException;
import java.io.InputStream;
import java.lang.Integer;
import java.lang.Object;
import java.lang.String;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;

import com.some.icr.client.dto.ICRFileMatter;
import com.some.icr.client.dto.ICRUploadDTO;
import com.some.icr.client.encode.EnCode;
import com.some.icr.client.exception.ICRClientException;
import com.some.um.client.util.StringUtil;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

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

    private void checkParamsNotInArray(String param) {
        if (!Arrays.asList("1", "2", "2", "3").contains(udto.getPriority())) {
            throw new ICRClientException("priority must be 0/1/2/3");
        }
    }

	public boolean uploadToICR(String username, String password, ICRUploadDTO udto) throws ICRClientException {
		HttpClient httpClient = null;
		PostMethod post = null;
		httpClient = new HttpClient();
		try {
			if (StringUtil.isEmpty(username))
				throw new ICRClientException("username can not be null");
			if (StringUtil.isEmpty(password))
				throw new ICRClientException("password can not be null");
			if (StringUtil.isEmpty(udto.getUrlPath()))
				throw new ICRClientException("urlPath can not be null");
			if (!udto.getPriority().equals("0") && !udto.getPriority().equals("1")
					&& !udto.getPriority().equals("2") && !udto.getPriority().equals("3"))
				throw new ICRClientException("priority must be 0/1/2/3");
			if (StringUtil.isEmpty(udto.getDocumentId()))
				throw new ICRClientException("documentId can not be null");
			if (StringUtil.isEmpty(udto.getTemplateId()))
				throw new ICRClientException("templateId can not be null");
			if (StringUtil.isEmpty(udto.getPages()))
				throw new ICRClientException("pages can not be null");
			if (StringUtil.isEmpty(udto.getReturnWay()))
				throw new ICRClientException("returnWay can not be null");
			if (udto.getReturnWay().equals("0")) {
				if (StringUtil.isEmpty(udto.getReturnAddr())) {
					throw new ICRClientException("returnAddr can not be null");
				}
			}
			EnCode ec = EnCode.getInstance();
			XStream xs = new XStream(new DomDriver());
			String str = xs.toXML(udto);
			post = new PostMethod(uploadUrl);
			post.addParameter("username", username);
			post.addParameter("password", ec.encryptionString(password));
			post.addParameter("str", str);
			int code = 0;
			try {
				code = httpClient.executeMethod(post);
			} catch (HttpException e) {
				throw new ICRClientException(e.getMessage(), e);
			} catch (IOException e) {
				throw new ICRClientException(e.getMessage(), e);
			}
			if (code == 200) {
				try {
					if (post.getResponseBodyAsString().equals("ok")) {
						return true;
					}
				} catch (IOException e) {
					throw new ICRClientException(e.getMessage(), e);
				}
				return false;
			}else if(code == 500){
				throw new ICRClientException(post.getResponseBodyAsString());
			}else{
				throw new ICRClientException(code+":"+post.getStatusText());
			}
		} catch (Exception e) {
			throw new ICRClientException(e.getMessage(), e);
		} finally {
			if (post != null) {
				post.releaseConnection();
			}
			if (httpClient != null) {
				httpClient.getHttpConnectionManager().closeIdleConnections(0);
				httpClient = null;
			}
		}
	}

	public List checkForTask(String username, String password,List list)
			throws ICRClientException {
		HttpClient httpClient = null;
		PostMethod post = null;
		httpClient = new HttpClient();
		List resultlist = null;
		try {
			if (StringUtil.isEmpty(username))
				throw new ICRClientException("username can not be null");
			if (StringUtil.isEmpty(password))
				throw new ICRClientException("password can not be null");
			if (list == null || list.size() == 0)
				throw new ICRClientException("list can not be null");
			Iterator ite = list.iterator();
			while(ite.hasNext()){
				ICRUploadDTO udto = (ICRUploadDTO) ite.next();
				if (StringUtil.isEmpty(udto.getUrlPath()))
					throw new ICRClientException("urlPath can not be null");
				if (!udto.getPriority().equals("0") && !udto.getPriority().equals("1")
						&& !udto.getPriority().equals("2") && !udto.getPriority().equals("3"))
					throw new ICRClientException("priority must be 0/1/2/3");
				if (StringUtil.isEmpty(udto.getDocumentId()))
					throw new ICRClientException("documentId can not be null");
			}
			
			XStream xs = new XStream(new DomDriver());
			String strlist = xs.toXML(list);
			EnCode ec = EnCode.getInstance();
			post = new PostMethod(checkUrl);
			post.addParameter("username", username);
			post.addParameter("password", ec.encryptionString(password));
			post.addParameter("strlist", strlist);
			int code = 0;
			try {
				code = httpClient.executeMethod(post);
			} catch (HttpException e) {
				throw new ICRClientException(e.getMessage(), e);
			} catch (IOException e) {
				throw new ICRClientException(e.getMessage(), e);
			}
			if (code == 200) {
				String resultString = null;
				try {
					resultString = post.getResponseBodyAsString();
				} catch (IOException e) {
					throw new ICRClientException(e.getMessage(), e);
				}
				resultlist = (List) xs.fromXML(resultString);
				return resultlist;
			}else if(code == 500){
				throw new ICRClientException(post.getResponseBodyAsString());
			}else{
				throw new ICRClientException(code+":"+post.getStatusText());
			}
		} catch (Exception e) {
			throw new ICRClientException(e.getMessage(), e);
		} finally {
			if (post != null) {
				post.releaseConnection();
			}
			if (httpClient != null) {
				httpClient.getHttpConnectionManager().closeIdleConnections(0);
				httpClient = null;
			}
		}
	}
	
	public ICRFileMatter downloadFromICR(String username,
			String password,ICRUploadDTO udto) throws ICRClientException {
		HttpClient httpClient = null;
		PostMethod post = null;
		httpClient = new HttpClient();
		ICRFileMatter fm = null;
		byte[] content = null;
		try {
			if (StringUtil.isEmpty(username))
				throw new ICRClientException("username can not be null");
			if (StringUtil.isEmpty(password))
				throw new ICRClientException("password can not be null");
			if (udto == null)
				throw new ICRClientException("ICRUploadDTO can not be null");
			if (StringUtil.isEmpty(udto.getUrlPath()))
				throw new ICRClientException("urlPath can not be null");
			if (!udto.getPriority().equals("0") && !udto.getPriority().equals("1")
					&& !udto.getPriority().equals("2") && !udto.getPriority().equals("3"))
				throw new ICRClientException("priority must be 0/1/2/3");
			if (StringUtil.isEmpty(udto.getDocumentId()))
				throw new ICRClientException("documentId can not be null");
			EnCode ec = EnCode.getInstance();
			XStream xs = new XStream(new DomDriver());
			String str = xs.toXML(udto);
			post = new PostMethod(downloadUrl);
			post.addParameter("username", username);
			post.addParameter("password", ec.encryptionString(password));
			post.addParameter("str", str);
			int code = 0;
			try {
				code = httpClient.executeMethod(post);
			} catch (HttpException e) {
				throw new ICRClientException(e.getMessage(), e);
			} catch (IOException e) {
				throw new ICRClientException(e.getMessage(), e);
			}
			if (code == 200) {
				try {
					content = readData(post.getResponseBodyAsStream());
				} catch (IOException e) {
					throw new ICRClientException(e.getMessage(), e);
				}
				fm = new ICRFileMatter();
				fm.setFileContent(content);
				fm.setFilename(post.getResponseHeader("filename").getValue());
				return fm;
			}else if(code == 500){
				throw new ICRClientException(post.getResponseBodyAsString());
			}else{
				throw new ICRClientException(code+":"+post.getStatusText());
			}
		} catch (Exception e) {
			throw new ICRClientException(e.getMessage(), e);
		} finally {
			if (post != null) {
				post.releaseConnection();
			}
			if (httpClient != null) {
				httpClient.getHttpConnectionManager().closeIdleConnections(0);
				httpClient = null;
			}
		}
	}

	private byte[] readData(InputStream ins) throws IOException {
		byte[] buf = new byte[2048];
		int count = 0;
		int len = 0;
		byte data[] = new byte[2048];
		byte[] result = null;
		try {
			while ((len = ins.read(data, 0, 2048)) != -1) {
				int newcount = count + len;
				if (newcount > buf.length) {
					byte newbuf[] = new byte[Math
							.max(buf.length << 1, newcount)];
					System.arraycopy(buf, 0, newbuf, 0, count);
					buf = newbuf;
				}
				System.arraycopy(data, 0, buf, count, len);
				count = newcount;
			}
			result = new byte[count];
			System.arraycopy(buf, 0, result, 0, count);

		} finally {
			ins.close();
		}
		return result;
	}
	
}
