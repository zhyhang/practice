/**
 * 
 */
package com.zyh.test.j2se;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import jodd.lagarto.dom.Document;
import jodd.lagarto.dom.LagartoDOMBuilder;
import jodd.lagarto.dom.Node;

/**
 * 
 * using for temporary test,try,output...
 * 
 * @author zhyhang
 *
 */
public class MainTemporary {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		extractFilePage();
//		extractHttpPage();

	}
	
	private static void extractFilePage() throws Exception{
		String pyadPage = new String(Files.readAllBytes(Paths.get(MainTemporary.class.getResource("/iframe_dynamic_create.html").toURI())),StandardCharsets.UTF_8);
		System.out.printf("original page content:\n%s\nencoded:\n%s\n", pyadPage,
				Base64.getEncoder().encodeToString(pyadPage.getBytes(StandardCharsets.UTF_8)));

	}

	private static void extractHttpPage() throws IOException, ClientProtocolException {
		// get pyadIframe code and encode with base64(java8)
		String pyadViewUrl ="http://stats.ipinyou.com/cpbox.html?rdm=vg9lHhk&pypk=gDHH.l9.HYnSZrkmOej8kQLh5OLpGDEAgunHJoaDvgjTornD0yneSoEQygCuWX.DM9lHhk.6cLlOwUQ-5fOL0fhfISZE_.H.HSCSVx5JG6CyXMkKeON7G0.I.psbNTKE&pyre=http%3A%2F%2Fstats.ipinyou.com%2Fbaidu%2Fclick%3Fs%3DrSBxQ_sLsbi5.HSCSVx5JG6CyXMkKeON7G0.DM9lHhk.6S1FerlrOvMUDQcWlOmSUx5RjeEZJIMhvh8e4pEQIyT6hsRqyYzM6p2uZ6fuiIk06gjyVDk7Qvk4god0tSkzQp5y-vEC0oNh7FRD-ApiYNS6_ZsOAFsP6DzJ_q8bisCm.Vg5OLvT2PHTvXV5o.W.Ag8XdEvj6m.QA.W.kAdSQAcEd9d01m1YuSMyvx5zM9FnSN5H7r9LNDc1YyRerZbgxgEaWr8F_5mcLhTv.I.K.W.Q.W.2On0ZrC8Ovn8DoB_egLiBDj2g3.Ip.y.W.l9.8Ks.2L_.DN5.gDHH._EM.H.AWfM._.z.kAdSQAcEdIq7jMp45rSfc5d70o8iYd56u19Cm5mdf993ibuDiYjXfrkaWqKMOdSU31mBexsuGe9xaM59JFqztx5KYqNi0QC_71qfWyNdSx8ZW-mWWXKKpapipx2QpYdm5L9Eqy57Q9lAJOmbv11EoKN60qTRSmmsRrSZmxqLsT8Vih9eKF9OT6SDCT1wpO9rDrsgeyC.f.bWnMhK.tDTJQuxEGTqSOsuYuSF_vuz.5F1RQup0Bx19Xg5s.tl.uYYbv.umYbv.MC.SWdYL3Q28TCmLrmQcHSUh_&pymk=http%3A%2F%2Fclick%2Ebes%2Ebaidu%2Ecom%2Fadx%2Ephp%3Fc%3Dcz1jYTNiYzQ3YjdjMjE5ZmU3AHQ9MTQ0MjE5MzU5NABzZT0xAGJ1PTY0MTgwNDEAdHU9OTIyMzM3MjAzMjU2MTE2ODQ3MABhZD01MzE2ODY3AHNpdGU9aHR0cDovL2JqLmJlbmRpYmFvLmNvbS9uZXdzLzIwMTQxMjE1LzE3NDIwNi5zaHRtAHY9MQBpPTMzYjAwODI3%26k%3Ddz02NDAAaD02MABjc2lkPTI1NzY5ODAzODQwMAB0bT0xMDc5NjY3AHRkPTAAd2k9NjQxODA0MQBmbj1oYW9odV9jcHIAZmFuPQB1aWQ9Njk0MTgzNABjaD0wAG9zPTE3AGJyPTEwAGlwPTIyMy4yMjMuMTk5LjEyMABzc3A9MQBhcHBfaWQ9AGFwcF9zaWQ9AHNka192ZXJzaW9uPQB0dHA9MQBjb21wbGU9MABzdHlwZT0wAGNobWQ9MABzY2htZD0wAGFkY2xhc3M9MA%26url%3D"; 
				//"http://stats.ipinyou.com/cpbox.html?rdm=PkOGnhk&pypk=ZWd2.kPs.6OfxCAL8OejSDQCzegahGrRsgvlTaojdUOjToralYyfeRoR5yhbvWDLu_vfFAZfNHOkmZJzmOeC93Z2AeYk9QJkpje8ZJoRpvYj1bJnt0vE.T1OGnhk.MGctAVq0wkgvsMkLmQveSX.h.6Kz7VrTJOTlxDZ29tgF7GJuyjek2YdmIvgui-Dae06z.I.psbNTKEmdpR&pyre=http%3A%2F%2Fstats.ipinyou.com%2Ftanx%2Fclick%3Fp%3DrFBSQ_fLsbA5.6Kz7VrTJOTlxDZ29tgF7GJuyjek2YdmIvgui-Dae06z.T1OGnhk.p11t3xNiX8dxBZc6.W.hO9iWuhK.QA.W.kAdSQAcEd9d01m1YuSMyvx5zM9FnSN5H7r9LNDc1YyRerZflyh2jIDzusQKM3sb.I.K.W.s.W.51SBZDz8Ov2WDIdcehkiBJlZXcLHJobVvhzDbX.v_.Rh.kPs.gf_.PCA.ufl.ZWd2.2u8.h.Asfs._.z.id11OkT23l5aGB153MjC-vFhNZFn7YFf7Y1d8xqQsQ8e0OsMpzNQHpKQPTcrPhpNiJ1SqksaQq9xgMmEuYKSo6LAG9mHSdCe7JNdxeFZsZTkifu5AKNlHAR.f.dWnMhK.tDTJQuxEGTqSOsuYuSF_vuz.5F1RQup0Bx19Xg5s.zfs.ubqJEYc.AL%26s%3DtJEO5tdZ6ZFwXtQWkgQ_MP&pymk=http%3a%2f%2fclick.tanx.com%2fct%3ftanx_k%3d185%26tanx_e%3dlf1ih36peKnDuAM9Sfjfj%252bzEE%252bFCfF6Bo3O1zL1V3RV1VWqTgqfPlq8O81nyhCnE9s5IClWCo%252fEkf0nloKCUEqX3H9h77tFFOCPUni4GjVxVx7%252bdhDls4LZmByYOiN7UGJojW7NnQbgX5hW0JnnVh4XShgvYy1bC0cU5vddAcVchKGtFlSf70g%253d%253d%26tanx_u%3d"; 
				//"http://stats.ipinyou.com/cpbox.html?rdm=sD_YIsk&pypk=rJIH.kPs.6OzSCD2xPy2SDQCzegahGrRsgvlCJoEZcgcTorjQ06nq7QE5yhEMWA2FheRFAZ8S2hCRZDB.nO_YIsk.h6gAIFB0iLaE5cCiVD0Gg0.s.OzKpkpbSjNfrlsKqMYmgUkChae9Zot.I.psbNTKE&pyre=http%3A%2F%2Fstats.ipinyou.com%2Fgdn%2Fclick%3Fp%3DrFBSQ__LsbA5.OzKpkpbSjNfrlsKqMYmgUkChae9Zot.nO_YIsk.Z0KAD75CP85DQIm9e0p3FrbH0ZMFNM5VyXF2.W.hX8U54hF.QA.W.kAdSQAcEd9d01m1YuSMyvx5zM9FnSN5H7r9LNDc1YyRerZflyh2jIDzusQKM3sb.I.K.W.s.W.6OnrZrnROecy3U.v_.Rh._.p.kPs.gf_.PCA.ufl.rJIH.9Uu.s.Asfs._.z.id11OkT23l5aGB153MjC-vFhNZFn7YFf7Y1d8xqQsQ8e0OsMpzNQHpKQPTcrPhpNiJ1SqksaQq9xgMmEuYKSo6LAG9mHSdCe7JNdxeFZsZTkifu5AKNlHAR.f.dWnMhK.tDTJQuxEGTqSOsuYuSF_vuz.5F1RQup0Bx19Xg5s.q8.ubqJEYc.AL%26s%3DxTRLBE8UUVFj334T8Yvl2_&pymk=http://adclick.g.doubleclick.net/aclk%3Fsa%3DL%26ai%3DCfhsPW-LyVeDPDoSpvATc2LuwDfX_prkHzeSx9GXAjbcBEAEgAGCdudCBkAWCARdjYS1wdWItMzUyMjEzNjAwNTg3OTA2NcgBCakCSMUI8dgNhT6oAwGYBACqBJ8BT9DaGkl8_SupItgxvf-OOxXDuQP_E2_4fBL21GzbNBXzt1Jxz4CSpyIw9Dlh4Ip2gh2_72788gpjsn4LgVYlKWg7LdmEzq1vmYPl8P2UQgcheCzCVlMOMAMekB19cL5TGUFODktrmFFswrsfk8JPkEGLoljwCdmmZ2zUa_DI4DRPU49PH7Q-CCfEI5qWQD-ZObnxfJvU7RUtIvxyZgS0gAar6PL375n2pogBoAYh2AcA%26num%3D1%26sig%3DAOD64_2u8a-VUB7TmrM_AwUiK6E23Aq7uA%26client%3Dca-pub-3522136005879065%26adurl%3D";
		CloseableHttpResponse response = HttpClients.createDefault().execute(new HttpGet(pyadViewUrl));
		String pyadPage = EntityUtils.toString(response.getEntity());
		response.close();
		int bbi = pyadPage.indexOf("<body>");
		int bei = pyadPage.indexOf("</body>");

		LagartoDOMBuilder domBuilder = new LagartoDOMBuilder();
		Document doc = domBuilder.parse(pyadPage.substring(bbi + 6, bei));

		System.out.printf("original page content:\n%s\nparsed:\n%s\nencoded:\n%s\nbody encode:\n%s\n", pyadPage,
				doc.getHtml(), Base64.getEncoder().encodeToString(pyadPage.getBytes(StandardCharsets.UTF_8)),
				Base64.getEncoder().encodeToString(pyadPage.substring(bbi + 6, bei).getBytes(StandardCharsets.UTF_8)));
		
		/*
		System.out.println("Body:");
		Node[] childNodes = doc.getChildNodes();
		int count = 0;
		for (int i = 0; i < childNodes.length; i++) {
			String html = childNodes[i].getHtml();
			if (StringUtils.isNotBlank(html)) {
				System.out.println(count++);
				System.out.println(html);
				if (childNodes[i].getFirstChild() != null) {
					System.out.println(childNodes[i].getFirstChild().getNodeValue());
					System.out.println(Base64.getEncoder().encodeToString(
							childNodes[i].getFirstChild().getNodeValue().getBytes(StandardCharsets.UTF_8)));
				}
			}
		}
		System.out.println("Body(Base64):");
		for (int i = 0; i < childNodes.length; i++) {
			System.out.println(
					Base64.getEncoder().encodeToString(childNodes[i].getHtml().getBytes(StandardCharsets.UTF_8)));
		}
		*/
	}
	
	

}