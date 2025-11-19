(function () {
    const tg = window.Telegram.WebApp;
    const userId = tg.initDataUnsafe.user.id;
    const initData = tg.initData;

    const shopList = document.getElementById('shop-list');
    const shopListCompleted = document.getElementById('shop-list-completed');

    const overlay = document.getElementById('overlay');
    const completedPanel = document.getElementById('completedPanel');
    const completedHeader = document.getElementById('completedHeader');

    completedHeader.addEventListener('click', () => {
        const isOpen = completedPanel.classList.contains('open');
        completedPanel.classList.toggle('open');
        overlay.classList.toggle('active', !isOpen);
        document.body.style.overflow = !isOpen ? 'hidden' : '';
    });

    overlay.addEventListener('click', () => {
        completedPanel.classList.remove('open');
        overlay.classList.remove('active');
        document.body.style.overflow = '';
    });


    async function loadShopList() {
        try {
            const response = await fetch(`api/purchases/${userId}`, {
                method: 'GET',
                headers: {
                    'Authorization': `tma ${initData}`
                }
            });
            const purchases = await response.json();

            shopList.innerHTML = '';
            shopListCompleted.innerHTML = '';

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
                        const checkbox = event.target;
                        const item = checkbox.closest('li');
                        const checked = checkbox.checked;
                        const checkedBefore = !checkbox.checked;

                        moveItem(item, checked);
                        const response = await fetch(`api/toggle`, {
                            method: 'PATCH',
                            headers: {
                                'Authorization': `tma ${initData}`,
                                'Content-Type': 'application/json'
                            },
                            body: JSON.stringify({
                                id: checkbox.id,
                                completed: checked
                            })
                        });
                        if (!response.ok) {
                            checkbox.checked = checkedBefore;
                            moveItem(item, checkedBefore)
                            console.error('Не удалось обновить статус');
                        }
                    });

                    if (purchase.completed) {
                        shopListCompleted.appendChild(shopItem);
                    } else {
                        shopList.appendChild(shopItem);
                    }
                });
            }
        } catch (error) {
            console.log(error);
        }
    }

    function moveItem(li, completed) {
        li.classList.toggle('checked', completed);
        li.querySelector('.checkbox')?.classList.toggle('checked', completed);

        li.remove(); // убираем из текущего списка

        if (completed) {
            shopListCompleted.appendChild(li);
        } else {
            shopList.appendChild(li);
        }
    }

    tg.ready();
    loadShopList();
})();