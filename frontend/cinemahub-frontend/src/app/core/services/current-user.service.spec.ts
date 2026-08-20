import { TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it } from 'vitest';

import { CurrentUserService } from './current-user.service';

describe('CurrentUserService', () => {
  let service: CurrentUserService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CurrentUserService);
  });

  it('empieza sin usuario seteado', () => {
    expect(service.userId()).toBeNull();
  });

  it('setUserId actualiza el signal', () => {
    service.setUserId(5);
    expect(service.userId()).toBe(5);
  });
});