/*
 * Copyright 2014 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Contact details
 *
 * Email: se.solovyev@gmail.com
 * Site:  http://se.solovyev.org
 */

package org.solovyev.android.checkout;

import android.os.Bundle;
import android.os.RemoteException;
import com.android.vending.billing.IInAppBillingService;

import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;
import static org.solovyev.android.checkout.ProductTypes.IN_APP;
import static org.solovyev.android.checkout.ProductTypes.SUBSCRIPTION;
import static org.solovyev.android.checkout.RequestTestBase.newBundle;
import static org.solovyev.android.checkout.ResponseCodes.OK;

public class CheckoutInventoryTest extends InventoryTestBase {

	@Nonnull
	protected CheckoutInventory newInventory(@Nonnull Checkout checkout) {
		return new CheckoutInventory(checkout);
	}

	@Override
	protected boolean shouldVerifyPurchaseCompletely() {
		return true;
	}

	@Override
	protected void insertPurchases(@Nonnull String product, @Nonnull List<Purchase> purchases) throws RemoteException {
		insertPurchases(billing, product, purchases);
	}

	static void insertPurchases(@Nonnull Billing billing, @Nonnull String product, @Nonnull List<Purchase> purchases) throws RemoteException {
		final Bundle bundle = newBundle(OK);
		final ArrayList<String> list = new ArrayList<String>();
		for (Purchase purchase : purchases) {
			list.add(purchase.toJson());
		}
		bundle.putStringArrayList(Purchases.BUNDLE_DATA_LIST, list);
		final IInAppBillingService service = ((TestServiceConnector) billing.getConnector()).service;
		when(service.getPurchases(anyInt(), anyString(), eq(product), isNull(String.class))).thenReturn(bundle);
	}

	protected boolean isLoaded(@Nonnull Inventory inventory) {
		return ((BaseInventory) inventory).isLoaded();
	}

	@Test
	public void testIsLoadedWithEmptySkusList() throws Exception {
		populatePurchases();

		final Products products = Products.create()
				.add(IN_APP)
				.add(SUBSCRIPTION);
		final Checkout checkout = Checkout.forApplication(billing, products);

		final CheckoutInventory inventory = new CheckoutInventory(checkout);
		final TestListener listener = new TestListener();
		checkout.start();
		inventory.load().whenLoaded(listener);

		waitWhileLoading(inventory);

		final Inventory.Product app = listener.products.get(IN_APP);
		Assert.assertTrue(app.getSkus().isEmpty());
		Assert.assertFalse(app.getPurchases().isEmpty());
		final Inventory.Product sub = listener.products.get(SUBSCRIPTION);
		Assert.assertTrue(sub.getSkus().isEmpty());
		Assert.assertFalse(sub.getPurchases().isEmpty());
	}
}
