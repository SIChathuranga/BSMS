import { authorizedFetch } from './app.js';

const CART_KEY = 'BSMS_CART';

export function getCart() {
  return JSON.parse(localStorage.getItem(CART_KEY) || '[]');
}

export function addToCart(product, qty=1) {
  const cart = getCart();
  const idx = cart.findIndex(i => i.productId === product.id);
  if (idx >= 0) { cart[idx].quantity += qty; } else { cart.push({ productId: product.id, quantity: qty, name: product.name, price: product.price }); }
  localStorage.setItem(CART_KEY, JSON.stringify(cart));
}

export function clearCart() { localStorage.removeItem(CART_KEY); }

export async function checkout() {
  const cart = getCart();
  if (!cart.length) return;
  const res = await authorizedFetch('http://localhost:8082/api/user/orders', {
    method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(cart)
  });
  if (!res.ok) throw new Error('Checkout failed');
  clearCart();
  return await res.json();
}
