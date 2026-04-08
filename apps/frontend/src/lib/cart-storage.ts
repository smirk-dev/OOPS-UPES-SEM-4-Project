export type CartProductSnapshot = {
  productId: number;
  name: string;
  vendorShopName: string;
  category: string;
  vertical: string;
  mrp: number;
  currentPrice: number;
  stockStatus: string;
};

export type CartItem = {
  product: CartProductSnapshot;
  quantity: number;
};

type CartState = {
  items: CartItem[];
};

const STORAGE_KEY = "upes_cart";

function defaultState(): CartState {
  return { items: [] };
}

function readState(): CartState {
  if (typeof window === "undefined") {
    return defaultState();
  }

  const raw = window.localStorage.getItem(STORAGE_KEY);
  if (!raw) {
    return defaultState();
  }

  try {
    const parsed = JSON.parse(raw) as CartState;
    if (!Array.isArray(parsed.items)) {
      return defaultState();
    }
    return parsed;
  } catch {
    return defaultState();
  }
}

function writeState(state: CartState) {
  if (typeof window === "undefined") {
    return;
  }
  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
}

export const cartStorage = {
  getItems(): CartItem[] {
    return readState().items;
  },

  addItem(snapshot: CartProductSnapshot) {
    const state = readState();
    const existing = state.items.find((item) => item.product.productId === snapshot.productId);

    if (existing) {
      existing.quantity += 1;
    } else {
      state.items.push({ product: snapshot, quantity: 1 });
    }

    writeState(state);
    return state.items;
  },

  updateQuantity(productId: number, quantity: number) {
    const state = readState();
    const target = state.items.find((item) => item.product.productId === productId);

    if (!target) {
      return state.items;
    }

    if (quantity <= 0) {
      state.items = state.items.filter((item) => item.product.productId !== productId);
    } else {
      target.quantity = quantity;
    }

    writeState(state);
    return state.items;
  },

  removeItem(productId: number) {
    const state = readState();
    state.items = state.items.filter((item) => item.product.productId !== productId);
    writeState(state);
    return state.items;
  },

  clear() {
    writeState(defaultState());
  },

  getSummary() {
    const items = readState().items;

    const subtotal = items.reduce(
      (sum, item) => sum + item.product.currentPrice * item.quantity,
      0
    );
    const mrpTotal = items.reduce((sum, item) => sum + item.product.mrp * item.quantity, 0);
    const savings = Math.max(0, mrpTotal - subtotal);

    return {
      itemCount: items.reduce((sum, item) => sum + item.quantity, 0),
      subtotal,
      savings,
      payable: subtotal,
    };
  },
};
