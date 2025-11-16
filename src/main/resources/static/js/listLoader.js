(function () {
    const tg = window.Telegram.WebApp;
    const userId = tg.initDataUnsafe.user.id;
    const initData = tg.initData;

    async function loadShopList() {
        try {
            const response = await fetch(`api/purchases/${userId}`, {
                method: 'GET',
                headers: {
                    'Authorization': `tma ${initData}`
                }
            });
            const purchases = await response.json();

            const shopList = document.getElementById('shop-list');
            shopList.innerHTML = '';

            if (purchases.length === 0) {
                const shopItem = document.createElement('li');
                shopItem.classList.add('item');
                shopItem.innerHTML = `<label class="item-text">Пусто &#128549;</label></li>`;
                shopList.appendChild(shopItem);
            } else {
                purchases.forEach((purchase) => {
                    const shopItem = document.createElement('li');
                    shopItem.classList.add('item');
                    shopItem.innerHTML = `
                                <input id="${purchase.id}" type="checkbox" class="checkbox" ${purchase.completed ? 'checked' : ''}>
                                <label for="${purchase.id}" class="item-text">${purchase.product.name}</label>
                            `;

                    shopItem.addEventListener('change', async (event) => {
                        const response = await fetch(`api/toggle`, {
                            method: 'PATCH',
                            headers: {
                                'Authorization': `tma ${initData}`,
                                'Content-Type': 'application/json'
                            },
                            body: JSON.stringify({
                                id: event.target.id,
                                completed: event.target.checked
                            })
                        });
                        if (!response.ok) {
                            event.target.checked = !completed;
                            console.error('Не удалось обновить статус');
                        }
                    });
                    shopList.appendChild(shopItem);
                });
            }
        } catch (error) {
            console.log(error);
        }
    }

    tg.ready();
    loadShopList();
})();