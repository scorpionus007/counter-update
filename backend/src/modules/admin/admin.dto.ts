import { ApiProperty } from '@nestjs/swagger';

export class CloudDaySummaryDto {
  @ApiProperty()
  date!: string;

  @ApiProperty()
  unlockCount!: number;
}

export class AdminUserSummaryDto {
  @ApiProperty({ format: 'uuid' })
  userId!: string;

  @ApiProperty()
  name!: string;

  @ApiProperty()
  phone!: string;

  @ApiProperty()
  kycStatus!: string;

  @ApiProperty()
  createdAt!: string;

  @ApiProperty()
  todayCount!: number;

  @ApiProperty()
  last7DaysTotal!: number;

  @ApiProperty()
  totalEvents!: number;

  @ApiProperty({ type: [CloudDaySummaryDto] })
  recentDays!: CloudDaySummaryDto[];
}

export class AdminStatsDto {
  @ApiProperty()
  users!: number;

  @ApiProperty()
  devices!: number;

  @ApiProperty()
  dailySummaries!: number;

  @ApiProperty()
  unlockEvents!: number;
}

export class AdminDeviceDto {
  @ApiProperty({ format: 'uuid' })
  id!: string;

  @ApiProperty({ format: 'uuid' })
  userId!: string;

  @ApiProperty()
  androidDeviceId!: string;

  @ApiProperty()
  integrityVerdict!: string;

  @ApiProperty()
  lastSeenAt!: string;

  @ApiProperty()
  userName!: string;

  @ApiProperty()
  userPhone!: string;
}

export class AdminDailySummaryDto {
  @ApiProperty({ format: 'uuid' })
  userId!: string;

  @ApiProperty()
  userName!: string;

  @ApiProperty()
  userPhone!: string;

  @ApiProperty()
  date!: string;

  @ApiProperty()
  unlockCount!: number;

  @ApiProperty()
  payableAmountP!: number;

  @ApiProperty()
  capped!: boolean;

  @ApiProperty()
  computedAt!: string;
}

export class AdminUnlockEventDto {
  @ApiProperty({ format: 'uuid' })
  id!: string;

  @ApiProperty({ format: 'uuid' })
  userId!: string;

  @ApiProperty()
  userName!: string;

  @ApiProperty()
  ts!: string;

  @ApiProperty()
  source!: string;
}
